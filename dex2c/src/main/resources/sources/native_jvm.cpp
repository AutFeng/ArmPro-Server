#include "native_jvm.hpp"
#include <limits>

namespace native_jvm::utils {

    union __fi_conv {
        jfloat m_jfloat;
        jint m_jint;
    };

    jint cfi(jfloat f) {
        __fi_conv fi;
        fi.m_jfloat = f;
        return fi.m_jint;
    }

    jfloat cif(jint i) {
        __fi_conv fi;
        fi.m_jint = i;
        return fi.m_jfloat;
    }


    union __dl_conv {
        jdouble m_jdouble;
        jlong m_jlong;
    };

    jlong cdl(jdouble d) {
        __dl_conv dl;
        dl.m_jdouble = d;
        return dl.m_jlong;
    }

    jdouble cld(jlong l) {
        __dl_conv dl;
        dl.m_jlong = l;
        return dl.m_jdouble;
    }

    jclass boolean_array_class;
    jmethodID string_intern_method;

    void init_utils(JNIEnv *env) {
        jclass clazz = env->FindClass("[Z");
        boolean_array_class = (jclass) env->NewGlobalRef(clazz);
        env->DeleteLocalRef(clazz);
        jclass string_clazz = env->FindClass("java/lang/String");
        string_intern_method = env->GetMethodID(string_clazz, "intern", "()Ljava/lang/String;");
        env->DeleteLocalRef(string_clazz);
    }

    jobjectArray
    create_multidim_array(JNIEnv *env, jint count, jint *sizes, const char *class_name, int line) {
        if (count == 0)
            return nullptr;
        if (*sizes < 0) {
            throw_re(env, "java/lang/NegativeArraySizeException", "MULTIANEWARRAY size < 0", line);
            return nullptr;
        }
        jclass clazz = env->FindClass((std::string(count, '[') + std::string(class_name)).c_str());
        jobjectArray result_array = env->NewObjectArray(*sizes, clazz, nullptr);
        env->DeleteLocalRef(clazz);
        for (jint i = 0; i < *sizes; i++) {
            jobjectArray inner_array = create_multidim_array(env, count - 1, sizes + 1, class_name,line);
            env->SetObjectArrayElement(result_array, i, inner_array);
            env->DeleteLocalRef(inner_array);
        }
        return result_array;
    }

    jclass find_class_wo_static(JNIEnv *env, const char *class_name) {
        /*jclass thread_class = env->FindClass("java/lang/Thread");
        jmethodID current_thread_method = env->GetStaticMethodID(thread_class, "currentThread",
                                                                 "()Ljava/lang/Thread;");
        jobject current_thread = env->CallStaticObjectMethod(thread_class,current_thread_method);
        jmethodID get_context_classloader_method = env->GetMethodID(thread_class,
                                                                    "getContextClassLoader",
                                                                    "()Ljava/lang/ClassLoader;");
        jobject classloader = env->CallObjectMethod(current_thread,get_context_classloader_method);
        jclass classloader_class = env->FindClass("java/lang/ClassLoader");
        jstring class_name_string = env->NewStringUTF(class_name);
        env->DeleteLocalRef(current_thread);
        jmethodID load_class_method = env->GetMethodID(classloader_class, "loadClass",
                                                       "(Ljava/lang/String;)Ljava/lang/Class;");
        jclass clazz = (jclass) env->CallObjectMethod(classloader,load_class_method,class_name_string);
        env->DeleteLocalRef(classloader);
        env->DeleteLocalRef(classloader_class);
        env->DeleteLocalRef(class_name_string);
        return clazz;*/
        jclass clazz = env->FindClass(class_name);
        return clazz;
    }

    void throw_re(JNIEnv *env, const char *exception_class, const char *error, int line) {
        jclass exception_class_ptr = env->FindClass(exception_class);
        if (!exception_class_ptr)
            return;
        env->ThrowNew(exception_class_ptr,
                      ("\"" + std::string(error) + "\" on " + std::to_string(line)).c_str());
        env->DeleteLocalRef(exception_class_ptr);
    }

    void bastore(JNIEnv *env, jarray array, jint index, jint value) {
        if (env->IsInstanceOf(array, boolean_array_class))
            env->SetBooleanArrayRegion((jbooleanArray) array, index, 1, (jboolean *) (&value));
        else
            env->SetByteArrayRegion((jbyteArray) array, index, 1, (jbyte *) (&value));
    }

    jbyte baload(JNIEnv *env, jarray array, jint index) {
        jbyte ret_value;
        if (env->IsInstanceOf(array, boolean_array_class))
            env->GetBooleanArrayRegion((jbooleanArray) array, index, 1, (jboolean *) (&ret_value));
        else
            env->GetByteArrayRegion((jbyteArray) array, index, 1, (jbyte *) (&ret_value));
        return ret_value;
    }


    jlong cast_dl(jdouble value) {
        if (std::isnan(value))
            return 0;
        int exponent;
        std::frexp(value, &exponent);
        if (std::isfinite(value) && exponent <= 63)
            return (jlong) value;
        return std::signbit(value) ? std::numeric_limits<jlong>::min()
                                   : std::numeric_limits<jlong>::max();
    }

    jlong cast_fl(jfloat value) {
        if (std::isnan(value))
            return 0;
        int exponent;
        std::frexp(value, &exponent);
        if (std::isfinite(value) && exponent <= 63)
            return (jlong) value;
        return std::signbit(value) ? std::numeric_limits<jlong>::min()
                                   : std::numeric_limits<jlong>::max();
    }

    jint cast_di(jdouble value) {
        if (std::isnan(value))
            return 0;
        int exponent;
        std::frexp(value, &exponent);
        if (std::isfinite(value) && exponent <= 31)
            return (jint) value;
        return std::signbit(value) ? std::numeric_limits<jint>::min()
                                   : std::numeric_limits<jint>::max();
    }

    jint cast_fi(jfloat value) {
        if (std::isnan(value))
            return 0;
        int exponent;
        std::frexp(value, &exponent);
        if (std::isfinite(value) && exponent <= 31)
            return (jint) value;
        return std::signbit(value) ? std::numeric_limits<jint>::min()
                                   : std::numeric_limits<jint>::max();
    }

    void clear_refs(JNIEnv *env, std::unordered_set<jobject> &refs) {
        for (jobject ref : refs)
            if (env->GetObjectRefType(ref) == JNILocalRefType)
                env->DeleteLocalRef(ref);
        refs.clear();
    }

    jstring get_interned(JNIEnv *env, jstring value) {
        jstring result = (jstring) env->CallObjectMethod(value, string_intern_method);
        return result;
    }
}