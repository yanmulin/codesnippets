#include "btree.h"
#include <cassert>

namespace mulin {

BTreeValueType BTree::Lookup(const BTreeKeyType &key) {
    NodeType *u = root_.get();
    size_t i = SearchKey(u, key);
    while (u->keys_[i] != key && !u->is_leaf_) {
        u = u->children_[i].get();
        i = SearchKey(u, key);
    }
    return i < u->num_ && u->keys_[i] == key ? u->values_[i] : nullptr;
}

void BTree::Insert(const BTreeKeyType &key, const BTreeValueType &value) {
    if (root_->IsFull()) {
        SplitRoot();
    }
    assert(!root_->IsFull());
    InsertNotFull(root_.get(), key, value);
}

void BTree::Remove(const BTreeKeyType &key) {
    RemoveNotHalf(root_.get(), key);
}

std::vector<std::vector<BTreeKeyType>> BTree::Traverse() const {
    std::vector<std::vector<BTreeKeyType>> result;
    TraverseImpl(root_.get(), result);
    return result;
}

std::vector<BTreeKeyType> BTree::Scan() const {
    std::vector<BTreeKeyType> result;
    ScanImpl(root_.get(), result);
    return result;
}

void BTree::Split(NodeType *p, size_t i) {
    assert(p != nullptr);
    assert(!p->is_leaf_);
    assert(!p->IsFull());

    NodeType *child = p->children_[i].get();
    assert(child != nullptr);
    assert(child->IsFull());
    assert(i < child->num_);

    for (size_t j=p->num_;j>=i+1;j--) {
        p->keys_[j] = p->keys_[j-1];
        p->values_[j] = p->values_[j-1];
    }
    for (size_t j=p->num_+1;j>=i+2;j--) {
        p->children_[j] = std::move(p->children_[j-1]);
    }
    p->num_ ++;

    size_t m = child->num_ >> 1;
    p->keys_[i] = child->keys_[m];
    p->values_[i] = child->values_[m];
    p->children_[i+1] = AllocateNode();

    Redistribute(p->children_[i+1].get(), child);
    p->children_[i+1]->is_leaf_ = child->is_leaf_;
}

void BTree::SplitRoot() {
    assert(root_->IsFull());
    std::unique_ptr<NodeType> nroot = AllocateNode();

    size_t m = root_->num_ >> 1;
    nroot->keys_[0] = root_->keys_[m];
    nroot->values_[0] = root_->values_[m];
    nroot->children_[0] = std::move(root_);
    nroot->children_[1] = AllocateNode();
    nroot->num_ = 1;
    nroot->is_leaf_ = false;

    Redistribute(nroot->children_[1].get(), nroot->children_[0].get());
    nroot->children_[1]->is_leaf_ = nroot->children_[0]->is_leaf_;

    root_ = std::move(nroot);
}

void BTree::Redistribute(NodeType *dst, NodeType *src) {
    size_t m = src->num_ >> 1;
    for (size_t j=m+1;j<src->num_;j++) {
        dst->keys_[j-m-1] = std::move(src->keys_[j]);
        dst->values_[j-m-1] = std::move(src->values_[j]);
    }
    for (size_t j=m+1;j<src->num_+1;j++) {
        dst->children_[j-m-1] = std::move(src->children_[j]);
    }
    dst->num_ = m;
    src->num_ = m;
}

void BTree::InsertNotFull(NodeType *p, const BTreeKeyType &key, const BTreeValueType &value) {
    assert(p != nullptr);
    size_t i = SearchKey(p, key);
    if (p->is_leaf_) {
        InsertToNode(p, i, key, value);
        size_ ++;
        return;
    }
    if (p->children_[i]->IsFull()) {
        Split(p, i);
        if (key > p->keys_[i]) i ++;
    }
    InsertNotFull(p->children_[i].get(), key, value);
}

void BTree::InsertToNode(NodeType *p, size_t i, const BTreeKeyType &key, const BTreeValueType &value) {
    assert(p->is_leaf_);
    for (size_t j=p->num_;j>i;j--) {
        p->keys_[j] = p->keys_[j-1];
        p->values_[j] = p->values_[j-1];
    }
    p->keys_[i] = key;
    p->values_[i] = value;
    p->num_ ++;
}

std::unique_ptr<BTree::NodeType> BTree::AllocateNode() {
    page_count_ ++;
    return std::unique_ptr<NodeType>{new NodeType{t_, true}};
}

void BTree::RemoveNotHalf(NodeType *p, const BTreeKeyType &key) {
    size_t i = SearchKey(p, key);
    if (p->keys_[i] != key && p->is_leaf_) { // 1.
        return;
    } else if (p->keys_[i] == key && p->is_leaf_) { // 2.
        ShiftLeft(p, i);
        size_ --;
        return;
    } else if (p->keys_[i] == key && p->children_[i]->IsHalfFull()) { // 3. 
        NodeType *u = Rightmost(p->children_[i].get());
        assert(u->is_leaf_);
        assert(u->keys_[u->num_-1] <= key);

        std::swap(u->keys_[u->num_-1], p->keys_[i]);
        std::swap(u->values_[u->num_-1], p->values_[i]);
        u->num_ --;
        size_ --;
        return;
    } else if (p->keys_[i] == key && p->children_[i+1]->IsHalfFull()) { // 4.
        NodeType *u = Leftmost(p->children_[i+1].get());
        assert(u->is_leaf_);
        assert(u->keys_[u->num_-1] >= key);

        std::swap(u->keys_[0], p->keys_[i]);
        std::swap(u->values_[0], p->values_[i]);
        ShiftLeft(u);
        size_ --;
        return;
    } else if (p->keys_[i] == key) { // 5.
        Merge(p, i);
    } else if (i > 0 && !p->children_[i]->IsHalfFull() && p->children_[i-1]->IsHalfFull()) { // 6.
        NodeType *lchild = p->children_[i-1].get();
        NodeType *rchild = p->children_[i].get();

        ShiftRight(rchild);

        rchild->keys_[0] = p->keys_[i-1];
        rchild->values_[0] = p->values_[i-1];
        if (!rchild->is_leaf_) {
            rchild->children_[0] = std::move(lchild->children_[lchild->num_]);
        }
        p->keys_[i-1] = lchild->keys_[lchild->num_-1];
        p->values_[i-1] = lchild->values_[lchild->num_-1];
        lchild->num_ --;

    } else if (i < p->num_ && !p->children_[i]->IsHalfFull() && p->children_[i+1]->IsHalfFull()) { // 7.
        NodeType *lchild = p->children_[i].get();
        NodeType *rchild = p->children_[i+1].get();

        lchild->keys_[lchild->num_] = p->keys_[i];
        lchild->values_[lchild->num_] = p->values_[i];
        if (!lchild->is_leaf_) {
            lchild->children_[lchild->num_+1] = std::move(rchild->children_[0]);
        }
        lchild->num_ ++;

        p->keys_[i] = rchild->keys_[0];
        p->values_[i] = rchild->values_[0];

        ShiftLeft(rchild);

    } else if (i > 0 && !p->children_[i-1]->IsHalfFull() && !p->children_[i]->IsHalfFull()) { // 8.
        Merge(p, i - 1);
        i --;
    } else if (i < p->num_ && !p->children_[i]->IsHalfFull() && !p->children_[i+1]->IsHalfFull()) { // 9.
        Merge(p, i);
    }

    assert(p->children_[i]->IsHalfFull());
    RemoveNotHalf(p->children_[i].get(), key);
}


void BTree::Merge(NodeType *p, size_t i) {
    assert(i < p->num_);
    NodeType *u = p->children_[i].get();
    NodeType *v = p->children_[i+1].get();
    assert(u->num_ + v->num_ + 1 <= 2 * t_ - 1);

    u->keys_[u->num_] = p->keys_[i];
    u->values_[u->num_] = p->values_[i];
    for (size_t j=0;j<v->num_;j++) {
        u->keys_[u->num_ + j + 1] = v->keys_[j];
        u->values_[u->num_ + j + 1] = v->values_[j];
    }
    for (size_t j=0;!u->is_leaf_&&j<=v->num_;j++) {
        u->children_[u->num_+j+1] = std::move(v->children_[j]);
    }
    u->num_ += v->num_ + 1;

    p->children_[i+1] = std::move(p->children_[i]);
    ShiftLeft(p, i);
    page_count_ --;
}

BTree::NodeType *BTree::Leftmost(NodeType *u) {
    while (!u->is_leaf_) {
        u = u->children_[0].get();
    }
    return u;
}

BTree::NodeType *BTree::Rightmost(NodeType *u) {
    while (!u->is_leaf_) {
        u = u->children_[u->num_].get();
    }
    return u;
}

void BTree::TraverseImpl(NodeType *u, std::vector<std::vector<BTreeKeyType>> &result) const {
    std::vector<BTreeKeyType> v;
    for (size_t i=0;i<u->num_;i++) {
        v.emplace_back(u->keys_[i]);
    }
    result.push_back(v);

    for (size_t i=0;!u->is_leaf_ && i<=u->num_;i++) {
        TraverseImpl(u->children_[i].get(), result);
    }
}

void BTree::ScanImpl(NodeType *u, std::vector<BTreeKeyType> &result) const {
    for (size_t i=0;i<=u->num_;i++) {
        if (!u->is_leaf_) {
            ScanImpl(u->children_[i].get(), result);
        }
        if (i < u->num_) {
            result.emplace_back(u->keys_[i]);
        }
    }
}

size_t BTree::SearchKey(NodeType *u, const BTreeKeyType &key) {
    for (size_t i=0;i<u->num_;i++) {
        if (u->keys_[i] >= key) return i;
    }
    return u->num_;
}

void BTree::ShiftLeft(NodeType *u, size_t i) {
    for (size_t j=i;j<u->num_-1;j++) {
        u->keys_[j] = u->keys_[j+1];
        u->values_[j] = u->values_[j+1];
    }
    for (size_t j=i;!u->is_leaf_ && j<u->num_;j++) {
        u->children_[j] = std::move(u->children_[j+1]);
    }
    u->num_ --;
}

void BTree::ShiftRight(NodeType *u, size_t i) {
    for (size_t j=u->num_;j>i;j--) {
        u->keys_[j] = u->keys_[j-1];
        u->values_[j] = u->values_[j-1];
    }
    for (size_t j=u->num_+1;!u->is_leaf_ && j>i;j--) {
        u->children_[j] = std::move(u->children_[j-1]);
    }
    u->num_ ++;
}

}