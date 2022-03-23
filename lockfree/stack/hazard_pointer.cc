#include "hazard_pointer.h"
#include <cassert>

hazard_pointer_manager::hazard_pointer 
    hazard_pointer_manager::hps_[MAX_HAZARD_POINTERS];

hazard_pointer_manager::hazard_pointer_manager(): to_be_reclaimed_(nullptr) {
    for (size_t i=0;i<MAX_HAZARD_POINTERS;i++) {
        hps_[i].ptr.store(nullptr);
        hps_[i].id.store(std::thread::id{});
    }
}

hazard_pointer_manager::~hazard_pointer_manager() {
    assert(to_be_reclaimed_.load() == nullptr);
    for (size_t i=0;i<MAX_HAZARD_POINTERS;i++) {
        assert(hps_[i].id.load() == std::thread::id{});
        assert(hps_[i].ptr.load() == nullptr);
    }
}

std::atomic<void*>& hazard_pointer_manager::get_hazard_pointer_for_current_thread() {
    thread_local static hp_owner hp;
    return hp.get_pointer();
}

bool hazard_pointer_manager::any_outstanding_hazard_pointer_for(void *ptr) const {
    for (size_t i=0;i<MAX_HAZARD_POINTERS;i++) {
        if (hps_[i].ptr.load() == ptr) {
            return true;
        }
    }
    return false;
}

void hazard_pointer_manager::delete_nodes_without_hazards() {
    pointer_to_reclaim *u = to_be_reclaimed_.exchange(nullptr);
    while (u) {
        pointer_to_reclaim *next = u->next;
        if (any_outstanding_hazard_pointer_for(u->ptr)) {
            add_to_reclaim_list(u);
        } else {
            delete u;
        }
        u = next;
    }
}

void hazard_pointer_manager::add_to_reclaim_list(pointer_to_reclaim *p) {
    p->next = to_be_reclaimed_.load();
    while (!to_be_reclaimed_.compare_exchange_strong(p->next, p));
}