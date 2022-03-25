#include <memory>
#include <atomic>
#include "stack.h"
#include "hazard_pointer.h"

template <typename T>
class stack_hp: public stack<T> {
public:
    stack_hp(): stack<T>(), head_(nullptr), hp_manager_() {}
    ~stack_hp() {
        Node *u = head_.exchange(nullptr);
        while (u != nullptr) {
            Node *next = u->next;
            delete u;
            u = next;
        }
    }

    std::shared_ptr<T> pop() override {
        std::atomic<void*>& hp = hp_manager_.get_hazard_pointer_for_current_thread();
        Node *old_head = head_.load();
        do {
            Node *tmp;
            do {
                tmp = old_head;
                hp.store(tmp);
                old_head = head_.load();
            } while (tmp != old_head);
        } while (old_head && !head_.compare_exchange_strong(old_head, old_head->next));
        hp.store(nullptr);
        std::shared_ptr<T> res;
        if (old_head) {
            res.swap(old_head->data);
            if (hp_manager_.any_outstanding_hazard_pointer_for(old_head)) {
                hp_manager_.reclaim_later(old_head);
            } else {
                delete old_head;
            }
            hp_manager_.delete_nodes_without_hazards();
        }
        return res;
    }

    void push(const T &data) override {
        Node *new_head = new Node(data);
        new_head->next = head_.load();
        while (!head_.compare_exchange_strong(new_head->next, new_head));
    }

private:

    struct Node {
        std::shared_ptr<T> data;
        Node *next;
        Node(const T &data_)
            : data(std::make_shared<T>(data_)), next(nullptr) {}
    };

    std::atomic<Node*> head_;
    hazard_pointer_manager hp_manager_;
};
