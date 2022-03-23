#include "bptree.h"
#include <cassert>
#include <cstdio>

namespace mulin {

size_t BPTreeNode::SearchKeyGT(const BPTreeKeyType &key) {
    for (size_t i=0;i<num_;i++) {
        if (keys_[i] > key) return i;
    }
    return num_;
}

BPTreeNode *BPTreeNode::LeftSibling() const {
    assert(parent_ != nullptr);
    size_t i = parent_->SearchKeyGT(keys_[0]);
    assert(parent_->children_[i] == this);
    if (i == 0) return nullptr;
    else return parent_->children_[i-1];
}

BPTreeNode *BPTreeNode::RightSibling() const {
    assert(parent_ != nullptr);
    size_t i = parent_->SearchKeyGT(keys_[0]);
    assert(parent_->children_[i] == this);
    if (i == parent_->num_) return nullptr;
    else return parent_->children_[i+1];
}

BPTreeNode::~BPTreeNode() {
    for (size_t i=0;!is_leaf_ && i<=num_;i++) {
        delete children_[i];
    }
}

BPlusTree::~BPlusTree() {
    delete root_;
}

void *BPlusTree::Lookup(const BPTreeKeyType &key) {
    NodeType *u = SearchLeaf(root_, key);
    size_t i = u->SearchKeyGT(key);
    return i > 0 && u->keys_[i-1] == key ? (void*)u->children_[i-1] : nullptr;
}

void BPlusTree::Insert(const BPTreeKeyType &key, void *data_ptr) {
    Verify(root_);

    NodeType *u = SearchLeaf(root_, key);
    assert(u->is_leaf_);

    size_t i = u->SearchKeyGT(key);
    ShiftRight(u, i);
    u->keys_[i] = key;
    u->children_[i] = (NodeType*)data_ptr;
    assert(i != u->num_);

    if (u->IsFull() && u == root_) {
        SplitRoot();
    } else if (u->IsFull()) {
        size_t m = u->num_ >> 1;
        InsertInternal(u->parent_, u->keys_[m]);
    }

    size_ ++;

    Verify(root_);
}

void BPlusTree::InsertInternal(NodeType *u, const BPTreeKeyType &key) {

    Verify(root_);

    assert(!u->is_leaf_);
    size_t i = u->SearchKeyGT(key);

    ShiftRight(u, i);
    u->keys_[i] = key;
    u->children_[i] = AllocateNode(u);
    std::swap(u->children_[i], u->children_[i+1]);

    assert(u->children_[i]->IsFull());
    assert(u->children_[i]->keys_[u->children_[i]->num_ / 2] == key);
    Split(u->children_[i], u->children_[i+1]);
    
    Verify(root_);

    if (u->IsFull() && u == root_) {
        SplitRoot();
    } else if (u->IsFull()) {
        size_t m = u->num_ >> 1;
        InsertInternal(u->parent_, u->keys_[m]);
    }

    Verify(root_);
}

void BPlusTree::Remove(const BPTreeKeyType &key) {
    Verify(root_);

    NodeType *u = SearchLeaf(root_, key);
    assert(u != nullptr && u->is_leaf_);
    size_t i = u->SearchKeyGT(key);
    if (i == 0 || u->keys_[i-1] != key) { // key not exists
        return;
    }
    size_ --;
    ShiftLeft(u, i - 1);
    
    if (u != root_ && u->IsUnderflown()) {
        NodeType *parent = u->parent_;
        NodeType *left_sibling = u->LeftSibling();
        NodeType *right_sibling = u->RightSibling();
        assert(!left_sibling || left_sibling->is_leaf_);
        assert(!right_sibling || right_sibling->is_leaf_);

        assert(u->IsUnderflown());

        if (left_sibling && left_sibling->IsHalfFull()) { // 2. borrow from left sibling
            size_t j = parent->SearchKeyGT(left_sibling->keys_[0]);
            assert(parent->children_[j] == left_sibling);
            assert(parent->children_[j+1] == u);

            ShiftRight(u, 0);
            u->keys_[0] = left_sibling->keys_[left_sibling->num_-1];
            u->children_[0] = left_sibling->children_[left_sibling->num_-1];
            ShiftLeft(left_sibling, left_sibling->num_ - 1);

            parent->keys_[j] = u->keys_[0];
        } else if (right_sibling && right_sibling->IsHalfFull()) { // 3. borrow from right sibling
            size_t j = parent->SearchKeyGT(u->keys_[0]);
            assert(parent->children_[j] == u);
            assert(parent->children_[j+1] == right_sibling);

            ShiftRight(u, u->num_);
            u->keys_[u->num_-1] = right_sibling->keys_[0];
            u->children_[u->num_-1] = right_sibling->children_[0];

            ShiftLeft(right_sibling, 0);
            parent->keys_[j] = right_sibling->keys_[0];
        } else if (left_sibling) { // 4. merge with left sibling
            size_t j = parent->SearchKeyGT(left_sibling->keys_[0]);
            assert(parent->children_[j] == left_sibling);
            Merge(left_sibling, u);
            RemoveInternal(parent, j);
            u = left_sibling;
        } else if (right_sibling) { // 5. merge with right sibling
            size_t j = parent->SearchKeyGT(u->keys_[0]);
            assert(parent->children_[j] == u);
            Merge(u, right_sibling);
            RemoveInternal(parent, j);
        } else assert(false);

        // assert(!u->IsUnderflown());
    }

    if (u != root_ && i - 1 == 0) {
        size_t j = u->parent_->SearchKeyGT(u->keys_[0]);
        assert(u->parent_->children_[j] == u);
        if (j > 0) {
            u->parent_->keys_[j-1] = u->keys_[0];
        }
    }

    Verify(root_);
}

// Remove keys[i] and children[i]
void BPlusTree::RemoveInternal(NodeType *u, size_t i) {

    assert(!u->is_leaf_);
    assert(u->children_[i+1]->num_ == 0);
    assert(u->children_[i]->num_ > 0);;
    delete u->children_[i+1];
    page_count_ --;
    u->children_[i+1] = u->children_[i];
    ShiftLeft(u, i);

    if (u != root_ && u->IsUnderflown()) {
        NodeType *parent = u->parent_;
        NodeType *left_sibling = u->LeftSibling();
        NodeType *right_sibling = u->RightSibling();
        assert(!left_sibling || !left_sibling->is_leaf_);
        assert(!right_sibling || !right_sibling->is_leaf_);

        if (left_sibling && left_sibling->IsHalfFull()) { // 7.
            size_t j = parent->SearchKeyGT(left_sibling->keys_[0]);
            assert(parent->children_[j] == left_sibling);
            assert(parent->children_[j+1] == u);

            ShiftRight(u, 0);
            u->keys_[0] = parent->keys_[j];
            u->children_[0] = left_sibling->children_[left_sibling->num_];
            u->children_[0]->parent_ = u;

            parent->keys_[j] = left_sibling->keys_[left_sibling->num_-1];
            ShiftLeft(left_sibling, left_sibling->num_-1);

        } else if (right_sibling && right_sibling->IsHalfFull()) { // 8.
            size_t j = parent->SearchKeyGT(u->keys_[0]);
            assert(parent->children_[j] == u);
            assert(parent->children_[j+1] == right_sibling);

            u->num_ ++;
            u->keys_[u->num_-1] = parent->keys_[j];
            u->children_[u->num_] = right_sibling->children_[0];
            u->children_[u->num_]->parent_ = u;
            parent->keys_[j] = right_sibling->keys_[0];
            ShiftLeft(right_sibling, 0);

        } else if (left_sibling) { // 9.
            size_t j = parent->SearchKeyGT(left_sibling->keys_[0]);
            assert(parent->children_[j] == left_sibling);
            MergeInternal(parent, j);
            RemoveInternal(parent, j);
            u = left_sibling;
        } else if (right_sibling) { // 10.
            size_t j = parent->SearchKeyGT(u->keys_[0]);
            assert(parent->children_[j] == u);
            MergeInternal(parent, j);
            RemoveInternal(parent, j);
        } else assert(false);

        assert(!u->IsUnderflown());
    }

    if (u != root_) {
        size_t j = u->parent_->SearchKeyGT(u->keys_[0]);
        assert(u->parent_->children_[j] == u);
    }

    Verify(root_);
}

std::vector<std::vector<BPTreeKeyType>> BPlusTree::Traverse() const {
    std::vector<std::vector<BPTreeKeyType>> result;
    TraverseImpl(root_, result);
    return result;
}

std::vector<BPTreeKeyType> BPlusTree::Scan() const {
    std::vector<BPTreeKeyType> result;
    NodeType *u;
    for (u=root_;!u->is_leaf_;u=u->children_[0]);
    while (u != nullptr) {
        for (size_t i=0;i<u->num_;i++) {
            result.push_back(u->keys_[i]);
        }
        u = u->children_[u->num_];
    }

    return result;
}

void BPlusTree::TraverseImpl(NodeType *u, std::vector<std::vector<BPTreeKeyType>> &result) const {
    std::vector<BPTreeKeyType> v;
    for (size_t i=0;i<u->num_;i++) {
        v.emplace_back(u->keys_[i]);
    }
    result.push_back(v);

    for (size_t i=0;!u->is_leaf_ && i<=u->num_;i++) {
        assert(u->children_[i]);
        TraverseImpl(u->children_[i], result);
    }
}

void BPlusTree::SplitRoot() {
    size_t m = root_->num_ >> 1;
    NodeType *new_root = AllocateNode(nullptr);
    new_root->keys_[0] = root_->keys_[m];
    new_root->children_[0] = root_;
    new_root->children_[1] = AllocateNode(new_root);
    new_root->num_ = 1;
    new_root->is_leaf_ = false;
    root_->parent_ = new_root;
    root_ = new_root;

    Split(new_root->children_[0], new_root->children_[1]);
}

void BPlusTree::Split(NodeType *u, NodeType *v) {
    if (u->is_leaf_) {
        SplitLeaf(u, v);
    } else {
        SplitInternal(u, v);
    }
}

void BPlusTree::SplitLeaf(NodeType *u, NodeType *v) {
    assert(u->IsFull());
    assert(u->is_leaf_);
    size_t m = u->num_ >> 1;
    for (size_t i=m;i<u->num_;i++) {
        v->keys_[i-m] = u->keys_[i];
        v->children_[i-m] = u->children_[i];
        u->children_[i] = nullptr;
    }
    v->num_ = u->num_ - m;
    v->children_[v->num_] = u->children_[u->num_];
    u->children_[u->num_] = nullptr;
    v->is_leaf_ = true;
    v->parent_ = u->parent_;

    u->num_ = m;
    u->children_[u->num_] = v;

    Verify(root_);
    
}

void BPlusTree::SplitInternal(NodeType *u, NodeType *v) {
    assert(u->IsFull());
    assert(!u->is_leaf_);

    size_t m = u->num_ >> 1;
    for (size_t i=m+1;i<u->num_;i++) {
        v->keys_[i-m-1] = u->keys_[i];
        v->children_[i-m-1] = u->children_[i];
        v->children_[i-m-1]->parent_ = v;
        u->children_[i] = nullptr;
    }
    v->num_ = u->num_ - m - 1;
    v->children_[v->num_] = u->children_[u->num_];
    u->children_[u->num_] = nullptr;
    v->children_[v->num_]->parent_ = v;
    v->is_leaf_ = false;
    v->parent_ = u->parent_;

    u->num_ = m;

    Verify(root_);
}

void BPlusTree::Merge(NodeType *u, NodeType *v) {
    assert(u->num_ + v->num_ <= 2 * t_ - 1);
    for (size_t i=0;i<v->num_;i++) {
        u->keys_[i+u->num_] = v->keys_[i];
        u->children_[i+u->num_] = v->children_[i];
        v->children_[i] = nullptr;
        if (!u->is_leaf_) u->children_[i+u->num_]->parent_ = u;
    }
    u->num_ += v->num_;
    u->children_[u->num_] = v->children_[v->num_];
    if (!u->is_leaf_) {
        u->children_[u->num_]->parent_ = u;
    }

    v->num_ = 0;
}

void BPlusTree::MergeInternal(NodeType *p, size_t i) {
    assert(i < p->num_);
    NodeType *u = p->children_[i];
    NodeType *v = p->children_[i+1];
    assert(!u->is_leaf_);
    assert(!v->is_leaf_);
    u->keys_[u->num_] = p->keys_[i];
    u->num_ ++;
    Merge(u, v);
}

BPlusTree::NodeType *BPlusTree::SearchLeaf(NodeType *u, const BPTreeKeyType &key) {
    size_t i = u->SearchKeyGT(key);
    while (!u->is_leaf_) {
        u = u->children_[i];
        i = u->SearchKeyGT(key);
    }
    return u;
}

BPlusTree::NodeType *BPlusTree::AllocateNode(NodeType *parent) {
    page_count_ ++;
    return new NodeType{t_, parent, true};
}

void BPlusTree::ShiftRight(NodeType *u, size_t i) {
    assert(i <= u->num_);
    assert(u->num_ + 1 <= 2 * t_ - 1);
    u->children_[u->num_+1] = u->children_[u->num_];
    for (size_t j=u->num_;j>i;j--) {
        u->keys_[j] = u->keys_[j-1];
        u->children_[j] = u->children_[j-1];
    }
    u->num_ ++;
    
    u->children_[i] = nullptr;
}

void BPlusTree::ShiftLeft(NodeType *u, size_t i) {
    assert(i < u->num_);
    assert(u->num_ >= t_ - 1);
    for (size_t j=i;j<u->num_-1;j++) {
        u->keys_[j] = u->keys_[j+1];
        u->children_[j] = u->children_[j+1];
    }
    if (u->is_leaf_ || u->num_ == 1 || i < u->num_ - 1) {
        u->children_[u->num_-1] = u->children_[u->num_];
    }
    u->num_ --;
    u->children_[u->num_+1] = nullptr;
}

void BPlusTree::Verify(NodeType *u) {
    assert(u == root_ || u->num_ >= t_ - 1);
    assert(u->num_ <= 2 * t_ - 1);
    for (size_t i=1;i<u->num_;i++) {
        assert(u->keys_[i] >= u->keys_[i-1]);
    }
    for (size_t i=0;!u->is_leaf_ && i<=u->num_;i++) {
        assert(u->children_[i]);
        assert(u->children_[i]->parent_ == u);
        Verify(u->children_[i]);
    }
    assert(u == root_ || !u->is_leaf_ || !u->children_[u->num_] || u->children_[u->num_]->keys_[0] > u->keys_[0]);
}

}