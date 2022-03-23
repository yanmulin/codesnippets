#include "stack.h"
#include <memory>
#include <atomic>
#include <cassert>

template<typename T>
class stack_gc: public stack<T> {
public:
    stack_gc(): stack<T>(), head_(nullptr), to_be_deleted_(nullptr), threads_in_pop_(0) {}
    ~stack_gc() {
        assert(threads_in_pop_ == 0);
        delete_nodes(head_.exchange(nullptr));
        delete_nodes(to_be_deleted_.exchange(nullptr));
        assert(nodes_created == nodes_deleted);
    }

    virtual std::shared_ptr<T> pop() override {
        threads_in_pop_ ++;
        Node *old_head = head_.load();
        while (old_head && !head_.compare_exchange_strong(old_head, old_head->next));
        std::shared_ptr<T> res;
        if (old_head) {
            res.swap(old_head->data);
        }
        try_reclaim(old_head);
        return res;
    }

    virtual void push(const T &data) override {
        Node *new_head = new Node(data);
        new_head->next = head_.load();
        while (!head_.compare_exchange_strong(new_head->next, new_head));
    }

    static std::atomic<size_t> nodes_created;
    static std::atomic<size_t> nodes_deleted;

private:

    struct Node {
        std::shared_ptr<T> data;
        Node *next;
        Node(const T &data_)
            : data(std::make_shared<T>(data_)), next(nullptr) {
            nodes_created ++;
        }
        ~Node() { nodes_deleted ++; }
    };

    std::atomic<Node*> head_;
    std::atomic<Node*> to_be_deleted_;
    std::atomic<int> threads_in_pop_;

    void try_reclaim(Node* old_head) {
        if (threads_in_pop_ == 1) {
            // only itself in pop(), no other threads referring old_head
            Node *to_delete_nodes = to_be_deleted_.exchange(nullptr);
            if (--threads_in_pop_ == 0) {
                delete_nodes(to_delete_nodes);
            } else if (to_delete_nodes) {
                append_nodes_to_chain(to_delete_nodes);
            }
            delete old_head;
        } else {
            if (old_head) {
                append_node_to_chain(old_head);
            }
            threads_in_pop_ --;
        }
    }

    void delete_nodes(Node *u) {
        while (u) {
            Node *next = u->next;
            delete u;
            u = next;
        }
    }

    void append_nodes_to_chain(Node *first, Node *last) {
        assert(first != nullptr);
        assert(last != nullptr);
        last->next = to_be_deleted_.load();
        while (!to_be_deleted_.compare_exchange_strong(last->next, first));
    }

    void append_nodes_to_chain(Node *first) {
        assert(first != nullptr);
        Node *last = first;
        while (last->next) last = last->next;
        append_nodes_to_chain(first, last);
    }

    void append_node_to_chain(Node *node) {
        assert(node != nullptr);
        append_nodes_to_chain(node, node);
    }
};

template <typename T>
std::atomic<size_t> stack_gc<T>::nodes_created;
template <typename T>
std::atomic<size_t> stack_gc<T>::nodes_deleted;