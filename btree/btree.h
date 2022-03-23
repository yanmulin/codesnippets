#ifndef BTREE_H
#define BTREE_H

#include <memory>
#include <vector>

namespace mulin {

using BTreeKeyType = char;
using BTreeValueType = void*;

struct BTreeNode {
    size_t num_;
    size_t t_;
    std::vector<BTreeKeyType> keys_;
    std::vector<BTreeValueType> values_;
    std::vector<std::unique_ptr<BTreeNode>> children_;
    bool is_leaf_;

    BTreeNode(size_t t, int is_leaf)
    : num_(0), t_(t), keys_(2*t-1), values_(2*t-1), children_(2*t), 
        is_leaf_(is_leaf) {}

    bool IsFull() const {
        return num_ == 2 * t_ - 1;
    }

    bool IsHalfFull() const {
        return num_ >= t_;
    }
};

class BTree {
public:
    BTree(size_t t): t_(t), root_(AllocateNode()), size_(0), page_count_(1) {}

    BTreeValueType Lookup(const BTreeKeyType &key);

    void Insert(const BTreeKeyType &key, const BTreeValueType &value);
    void Remove(const BTreeKeyType &key);

    std::vector<std::vector<BTreeKeyType>> Traverse() const;
    std::vector<BTreeKeyType> Scan() const;

    size_t Size() const { return size_; }
    size_t PageCount() const { return page_count_; }

private:
    using NodeType = BTreeNode;

    std::unique_ptr<NodeType> AllocateNode();

    size_t SearchKey(NodeType *u, const BTreeKeyType &key);

    void Split(NodeType *p, size_t i);
    void SplitRoot();
    void InsertNotFull(NodeType *p, const BTreeKeyType &key, const BTreeValueType &value);
    void InsertToNode(NodeType *p, size_t i, const BTreeKeyType &key, const BTreeValueType &value);
    void Redistribute(NodeType *dst, NodeType *src);

    void RemoveNotHalf(NodeType *p, const BTreeKeyType &key);
    void Merge(NodeType *p, size_t i);
    NodeType *Leftmost(NodeType *u);
    NodeType *Rightmost(NodeType *u);

    void ShiftLeft(NodeType *u, size_t i=0);
    void ShiftRight(NodeType *u, size_t i=0);

    void TraverseImpl(NodeType *u, std::vector<std::vector<BTreeKeyType>> &v) const;

    void ScanImpl(NodeType *u, std::vector<BTreeKeyType> &v) const;

    const size_t t_;
    std::unique_ptr<NodeType> root_;
    size_t size_;
    size_t page_count_;
};

}

#endif