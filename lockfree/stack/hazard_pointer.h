#pragma once
#include <atomic>
#include <thread>
#include <functional>
#include <cassert>

constexpr size_t MAX_HAZARD_POINTERS = 50;

class hazard_pointer_manager {
public:

    hazard_pointer_manager();
    ~hazard_pointer_manager();

    std::atomic<void*>& get_hazard_pointer_for_current_thread();

    bool any_outstanding_hazard_pointer_for(void *ptr) const;

    template <typename T> void reclaim_later(T *ptr) {
        add_to_reclaim_list(new pointer_to_reclaim(ptr));
    }

    void delete_nodes_without_hazards();
private:

    

    struct hazard_pointer {
        std::atomic<std::thread::id> id;
        std::atomic<void*> ptr;
    };

    class hp_owner {
    public:
        hp_owner(const hp_owner&)=delete;
        hp_owner operator=(const hp_owner&)=delete;
        hp_owner(): hp_(nullptr) {
            for (size_t i=0;i<MAX_HAZARD_POINTERS;i++) {
                std::thread::id id;
                if (hps_[i].id.compare_exchange_strong(id, std::this_thread::get_id())) {
                    hp_ = &hps_[i];
                    break;
                }
            }
            assert(hp_);
        }
        ~hp_owner() {
            hp_->ptr.store(nullptr);
            hp_->id.store(std::thread::id{});
        }

        std::atomic<void*>& get_pointer() {
            return hp_->ptr;
        }
    private:
        hazard_pointer *hp_;
    };

    struct pointer_to_reclaim {
        void *ptr;
        pointer_to_reclaim *next;
        std::function<void(void*)> deleter;

        template <typename T>
        pointer_to_reclaim(T *p): 
            ptr(p), deleter(&do_delete<T>), next(nullptr) {}
        ~pointer_to_reclaim() {
            deleter(ptr);
        }
    };

    static hazard_pointer hps_[MAX_HAZARD_POINTERS];
    std::atomic<pointer_to_reclaim*> to_be_reclaimed_;

    void add_to_reclaim_list(pointer_to_reclaim *p);

    template <typename T>
    static void do_delete(void *ptr) {
        delete static_cast<T*>(ptr);
    }
};
