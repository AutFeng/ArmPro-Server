#include "native_jvm.hpp"
#include "native_jvm_output.hpp"

$includes

namespace native_jvm {

    typedef void (* reg_method)(JNIEnv *);

    reg_method reg_methods[$class_count];

    void register_for_class(JNIEnv *env, jclass, jint id) {
        reg_methods[id](env);
    }

    void prepare_lib(JNIEnv *env) {
        utils::init_utils(env);

$register_code

        char method_name[] = "registerNativesForClass";
        char method_desc[] = "(I)V";
        JNINativeMethod loader_methods[] = {
            { (char *) method_name, (char *) method_desc, (void *)&register_for_class }
        };
        env->RegisterNatives(env->FindClass("$native_dir/Loader"), loader_methods, 1);
    }
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    vm->GetEnv((void **)&env, JNI_VERSION_1_6);
    native_jvm::prepare_lib(env);
    return JNI_VERSION_1_6;
}
