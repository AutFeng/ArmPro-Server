package armadillo.call;


import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class InvokeDynamic {
    public static CallSite callSite(int index, String[] strings, MethodHandles.Lookup lookup) {
        CallSite callSite = null;
        try {
            String[] var = strings[index].split(":");
            MethodType type = MethodType.fromMethodDescriptorString(var[2], InvokeDynamic.class.getClassLoader());
            String action = var[3];
            if ("virtual".equals(action)) {
                try {
                    callSite = new ConstantCallSite(lookup.
                            findVirtual(Class.forName(var[0]), var[1], type));
                }catch (Exception e){
                    e.printStackTrace();
                    Class<?> clz = null;
                    for (Method method : Class.forName(var[0]).getMethods()) {
                        if (method.getName().equals(var[1])) {
                            clz = method.getDeclaringClass();
                            break;
                        }
                    }
                    if (clz == null) {
                        for (Method method : Class.forName(var[0]).getDeclaredMethods()) {
                            if (method.getName().equals(var[1])) {
                                clz = method.getDeclaringClass();
                                break;
                            }
                        }
                    }
                    callSite = new ConstantCallSite(lookup.
                            findVirtual(clz, var[1], type));
                }
            } else if ("static".equals(action)) {
                try {
                    callSite = new ConstantCallSite(lookup.
                            findStatic(Class.forName(var[0]), var[1], type));
                } catch (Exception e) {
                    e.printStackTrace();
                    Class<?> clz = null;
                    for (Method method : Class.forName(var[0]).getMethods()) {
                        if (method.getName().equals(var[1])) {
                            clz = method.getDeclaringClass();
                            break;
                        }
                    }
                    if (clz == null) {
                        for (Method method : Class.forName(var[0]).getDeclaredMethods()) {
                            if (method.getName().equals(var[1])) {
                                clz = method.getDeclaringClass();
                                break;
                            }
                        }
                    }
                    callSite = new ConstantCallSite(lookup.
                            findStatic(clz, var[1], type));
                }
            } else if ("super".equals(action)) {
                callSite = new ConstantCallSite(lookup.
                        findVirtual(Class.forName(var[0]), var[1], type));
            } else if ("constructor".equals(action)) {
                callSite = new ConstantCallSite(lookup.
                        findConstructor(Class.forName(var[0]), type));
            } else if ("get".equals(action)) {
                try {
                    callSite = new ConstantCallSite(lookup.
                            findGetter(Class.forName(var[0]), var[1], type.returnType()));
                } catch (Exception e) {
                    callSite = new ConstantCallSite(lookup.
                            findStaticGetter(Class.forName(var[0]), var[1], type.returnType()));
                }
            } else if ("set".equals(action)) {
                try {
                    callSite = new ConstantCallSite(lookup.
                            findSetter(Class.forName(var[0]), var[1], type.returnType()));
                } catch (Exception e) {
                    callSite = new ConstantCallSite(lookup.
                            findStaticSetter(Class.forName(var[0]), var[1], type.returnType()));
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return callSite;
    }
}
