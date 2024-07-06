#include "interpreter_switch.h"

static signed char pool[$size] = $value;

static signed char *get_pool() {
    return pool;
}

static signed char *str_pool = get_pool();

static VmField vmField[] = {
${field_key}
};
static VmMethod vmMethod[] = {
${method_key}
};
static VmType vmType[] = {
${type_key}
};


static const VmType *ResolveType(uint32_t idx) {
    switch (idx) {
${type_ids}
    }
    return nullptr;
}


static const VmField *ResolveField(uint32_t idx) {
    switch (idx) {
${field_ids}
    }
    return nullptr;
}


static const VmMethod *ResolveMethod(uint32_t idx) {
    switch (idx) {
${method_ids}
    }
    return nullptr;
}


static const char *ResolveStringUTF8(uint32_t idx) {
    switch (idx) {
${string_ids}
    }
    return nullptr;
}

static const VmResolver vmResolver = {
        .ResolveField = ResolveField,
        .ResolveMethod = ResolveMethod,
        .ResolveType = ResolveType,
        .ResolveString = ResolveStringUTF8,
};