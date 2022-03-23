#include <gtest/gtest.h>
#include <iostream>
#include <algorithm>
#include "btree.h"
#include "bptree.h"

namespace mulin {

/*

           KQ
    /      |       \
   BF      M       TW
 / | \    / \     / | \
A CDE H   L NP   RS V XYZ

*/

std::vector<char> my_keys = {'F', 'S', 'Q', 'K', 'C', 'L', 'H', 'T', 'V', 
    'W', 'M', 'R', 'N', 'P', 'A', 'B', 'X', 'Y', 'D', 'Z', 'E'};

template <typename T>
void PrintTraverse(const T &bt) {
    auto v = bt.Traverse();
    for (size_t i=0;i<v.size();i++) {
        std::cout << "{";
        for (size_t j=0;j<v[i].size();j++) {
            std::cout << "'" << v[i][j] << "'";
            if (j < v[i].size() - 1) std::cout << ",";
        }
        std::cout << "}";
        if (i < v.size() - 1) std::cout << ",";
    }
    std::cout << std::endl;
}

void PrintScan(const BTree &bt) {
    auto v = bt.Scan();
    for (size_t i=0;i<v.size();i++) {
        std::cout << v[i];
        if (i < v.size() - 1) std::cout << " ";
    }
    std::cout << std::endl;
}

template <typename T>
void BuildTree(T &bt, const std::vector<char> &keys) {
    for (size_t i=0;i<keys.size();i++) {
        bt.Insert(keys[i], (void*)i);
    }
}

TEST(BTreeTest, Insert) {
    BTree bt{2};
    BuildTree(bt, my_keys);
    EXPECT_EQ(my_keys.size(), bt.Size());

    std::vector<std::vector<BTreeKeyType>> v = {
        {'K', 'Q'},
        {'B', 'F'},
        {'A'},
        {'C', 'D', 'E'},
        {'H'},
        {'M'},
        {'L'},
        {'N', 'P'},
        {'T', 'W'},
        {'R', 'S'},
        {'V'},
        {'X', 'Y', 'Z'},
    };
    EXPECT_EQ(12, bt.PageCount());
    EXPECT_EQ(v, bt.Traverse());

    // Lookup
    EXPECT_NE(nullptr, bt.Lookup('K'));
    EXPECT_EQ(3, reinterpret_cast<size_t>(bt.Lookup('K')));
    EXPECT_NE(nullptr, bt.Lookup('W'));
    EXPECT_EQ(9, reinterpret_cast<size_t>(bt.Lookup('W')));
    EXPECT_NE(nullptr, bt.Lookup('C'));
    EXPECT_EQ(4, reinterpret_cast<size_t>(bt.Lookup('C')));
}

TEST(BTreeTest, Remove) {
    BTree bt{2};
    BuildTree(bt, my_keys);
    EXPECT_EQ(12, bt.PageCount());
    EXPECT_EQ(my_keys.size(), bt.Size());

    bt.Remove('Z');
    EXPECT_EQ(12, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 1, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('Z'));

    bt.Remove('N');
    EXPECT_EQ(12, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 2, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('N'));

    bt.Remove('H');
    EXPECT_EQ(11, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 3, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('H'));

    bt.Remove('R');
    bt.Remove('V');
    EXPECT_EQ(11, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 5, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('V'));

    bt.Remove('W');
    EXPECT_EQ(10, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 6, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('W'));

    bt.Remove('B');
    EXPECT_EQ(9, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 7, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('B'));

    bt.Remove('X');
    EXPECT_EQ(9, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 8, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('X'));

    bt.Remove('T');
    EXPECT_EQ(8, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 9, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('T'));
}

TEST(BTreeTest, Scan) {
    BTree bt{2};
    BuildTree(bt, my_keys);

    std::vector<char> sorted;
    std::copy(my_keys.begin(), my_keys.end(), std::back_inserter(sorted));
    std::sort(sorted.begin(), sorted.end());

    auto v = bt.Scan();
    for (size_t i=0;i<v.size();i++) {
        EXPECT_EQ(sorted[i], v[i]);
    }
}

TEST(BPlusTreeTest, Insert) {
    BPlusTree bt{2};
    BuildTree(bt, my_keys);
    std::vector<std::vector<BTreeKeyType>> v = {
        {'M','V'}, {'C','K'}, {'B'},{'A'},{'B'}, {'D','F'},
        {'C'},{'D','E'},{'F','H'},{'L'},{'K'},{'L'},{'S'},
        {'N','Q'},{'M'},{'N','P'},{'Q','R'},{'T'},{'S'},{'T'},
        {'X'},{'W'},{'V'},{'W'},{'Y'},{'X'},{'Y','Z'},
    };
    EXPECT_EQ(v, bt.Traverse());

    // Lookup
    EXPECT_NE(nullptr, bt.Lookup('K'));
    EXPECT_EQ(3, reinterpret_cast<size_t>(bt.Lookup('K')));
    EXPECT_NE(nullptr, bt.Lookup('W'));
    EXPECT_EQ(9, reinterpret_cast<size_t>(bt.Lookup('W')));
    EXPECT_NE(nullptr, bt.Lookup('C'));
    EXPECT_EQ(4, reinterpret_cast<size_t>(bt.Lookup('C')));
}

TEST(BPlusTreeTest, Scan) {
    BPlusTree bt{2};
    BuildTree(bt, my_keys);

    std::vector<char> sorted;
    std::copy(my_keys.begin(), my_keys.end(), std::back_inserter(sorted));
    std::sort(sorted.begin(), sorted.end());

    auto v = bt.Scan();
    for (size_t i=0;i<v.size();i++) {
        EXPECT_EQ(sorted[i], v[i]);
    }
}

TEST(BPlusTreeTest, Remove) {
    BPlusTree bt{2};
    BuildTree(bt, my_keys);

    bt.Remove('F'); // 1
    EXPECT_EQ(27, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 1, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('F'));

    bt.Remove('B'); // 4,8
    EXPECT_EQ(26, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 2, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('B'));

    bt.Remove('H'); // 2
    EXPECT_EQ(26, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 3, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('H'));

    bt.Remove('M'); // 3
    EXPECT_EQ(26, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 4, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('M'));

    bt.Remove('S'); // 5,7
    EXPECT_EQ(25, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 5, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('S'));

    bt.Remove('N'); // 5,9,10,7
    EXPECT_EQ(23, bt.PageCount());
    EXPECT_EQ(my_keys.size() - 6, bt.Size());
    EXPECT_EQ(nullptr, bt.Lookup('N'));
}


}