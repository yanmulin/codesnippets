#ifndef BPTREE_H
#define BPTREE_H

#include <vector>
#include <memory>

namespace mulin {

using BPTreeKeyType = char;

struct BPTreeNode {
    size_t num_;
    size_t t_;
    std::vector<BPTreeKeyType> keys_;
    std::vector<BPTreeNode*> children_;
    BPTreeNode *parent_;
    bool is_leaf_;

    BPTreeNode(size_t t, BPTreeNode *p, bool is_leaf): num_(0), t_(t), keys_(2*t-1),
        children_(2*t), parent_(p), is_leaf_(is_leaf) {}
    ~BPTreeNode();

    size_t SearchKeyGT(const BPTreeKeyType &key);

    BPTreeNode *LeftSibling() const;
    BPTreeNode *RightSibling() const;
    
    bool IsFull() const { return num_ >= 2 * t_ - 1; }

    bool IsHalfFull() const { return num_ >= t_; }

    bool IsUnderflown() const { return num_ < t_ - 1; }
};

class BPlusTree {
public:
    BPlusTree(size_t t): t_(t), root_(AllocateNode(nullptr)), size_(0), page_count_(1) {}
    ~BPlusTree();

    void *Lookup(const BPTreeKeyType &key);
    void Insert(const BPTreeKeyType &key, void *data_ptr);
    void Remove(const BPTreeKeyType &key);

    std::vector<BPTreeKeyType> Scan() const;
    std::vector<std::vector<BPTreeKeyType>> Traverse() const;

    size_t Size() const { return size_; }
    size_t PageCount() const { return page_count_; }
private:

    using NodeType = BPTreeNode;

    NodeType *AllocateNode(NodeType *parent);

    NodeType *SearchLeaf(NodeType *u, const BPTreeKeyType &key);
    void InsertInternal(NodeType *u, const BPTreeKeyType &key);
    void SplitRoot();
    void Split(NodeType *u, NodeType *v);
    void SplitLeaf(NodeType *u, NodeType *v);
    void SplitInternal(NodeType *u, NodeType *v);

    void RemoveInternal(NodeType *u, size_t i);
    void Merge(NodeType *u, NodeType *v);
    void MergeInternal(NodeType *p, size_t i);

    void ShiftRight(NodeType *u, size_t i);
    void ShiftLeft(NodeType *u, size_t i);
    size_t SearchKeyGT(NodeType *u, const BPTreeKeyType &key);

    void TraverseImpl(NodeType *u, std::vector<std::vector<BPTreeKeyType>> &result) const;

    void Verify(NodeType *u);

    const size_t t_;
    NodeType *root_;
    size_t size_;
    size_t page_count_;
};

}

#endif