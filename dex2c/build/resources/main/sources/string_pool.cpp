#include "string_pool.hpp"

namespace native_jvm::string_pool {
    static signed char pool[$size] = $value;

    signed char *get_pool() {
        return pool;
    }
}