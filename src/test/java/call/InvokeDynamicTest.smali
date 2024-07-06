.class public Lcall/InvokeDynamicTest;
.super Ljava/lang/Object;
.source "InvokeDynamicTest.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcall/InvokeDynamicTest$testsuper2;,
        Lcall/InvokeDynamicTest$testsuper;
    }
.end annotation


# static fields
.field private static strings:[Ljava/lang/String;

.field private static test2:[Ljava/lang/String;


# instance fields
.field private test:[Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .registers 0

    .prologue
    .line 15
    invoke-static {}, Lcall/InvokeDynamicTest;->Armadillo()V

    .line 16
    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 9
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method private static Armadillo()V
    .registers 3

    .prologue
    .line 27
    const/16 v0, 0x3e8

    new-array v0, v0, [Ljava/lang/String;

    sput-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    .line 28
    sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    const/4 v1, 0x0

    const-string v2, "java.text.SimpleDateFormat:parse:(Ljava/lang/String;)Ljava/util/Date;:virtual"

    aput-object v2, v0, v1

    .line 29
    sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    const/4 v1, 0x1

    const-string v2, "java.util.Date:getTime:()J:virtual"

    aput-object v2, v0, v1

    .line 30
    sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    const/4 v1, 0x2

    const-string v2, "java.util.Date:<init>:()V:constructor"

    aput-object v2, v0, v1

    .line 31
    sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    const/4 v1, 0x3

    const-string v2, "call.InvokeDynamicTest:test:()[Ljava/lang/String;:get"

    aput-object v2, v0, v1

    .line 32
    sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    const/4 v1, 0x4

    const-string v2, "call.InvokeDynamicTest:test:()[Ljava/lang/String;:set"

    aput-object v2, v0, v1

    .line 33
    sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    const/4 v1, 0x5

    const-string v2, "call.InvokeDynamicTest:test2:()[Ljava/lang/String;:get"

    aput-object v2, v0, v1

    .line 34
    sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    const/4 v1, 0x6

    const-string v2, "call.InvokeDynamicTest:test2:()[Ljava/lang/String;:set"

    aput-object v2, v0, v1

    .line 35
    sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    const/4 v1, 0x7

    const-string v2, "call.InvokeDynamicTest$testsuper:test:()I:super"

    aput-object v2, v0, v1

    .line 36
    return-void
.end method

.method private static callSite(I)Ljava/lang/invoke/CallSite;
    .registers 14
    .param p0, "index"    # I

    .prologue
    .line 39
    const/4 v2, 0x0

    .line 41
    .local v2, "callSite":Ljava/lang/invoke/CallSite;
    :try_start_1
    sget-object v9, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    aget-object v9, v9, p0

    const-string v10, ":"

    invoke-virtual {v9, v10}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v8

    .line 42
    .local v8, "var":[Ljava/lang/String;
    const/4 v9, 0x2

    aget-object v9, v8, v9

    invoke-static {}, Ljava/lang/ClassLoader;->getSystemClassLoader()Ljava/lang/ClassLoader;

    move-result-object v10

    invoke-static {v9, v10}, Ljava/lang/invoke/MethodType;->fromMethodDescriptorString(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;

    move-result-object v7

    .line 43
    .local v7, "type":Ljava/lang/invoke/MethodType;
    const/4 v9, 0x3

    aget-object v0, v8, v9

    .line 44
    .local v0, "action":Ljava/lang/String;
    const-string v9, "virtual"

    invoke-virtual {v9, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_3a

    .line 45
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;

    move-result-object v9

    const/4 v10, 0x0

    aget-object v10, v8, v10

    .line 46
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v8, v11

    invoke-virtual {v9, v10, v11, v7}, Ljava/lang/invoke/MethodHandles$Lookup;->findVirtual(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;

    move-result-object v9

    invoke-direct {v3, v9}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .local v3, "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 80
    .end local v0    # "action":Ljava/lang/String;
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .end local v7    # "type":Ljava/lang/invoke/MethodType;
    .end local v8    # "var":[Ljava/lang/String;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    :cond_39
    :goto_39
    return-object v2

    .line 47
    .restart local v0    # "action":Ljava/lang/String;
    .restart local v7    # "type":Ljava/lang/invoke/MethodType;
    .restart local v8    # "var":[Ljava/lang/String;
    :cond_3a
    const-string v9, "static"

    invoke-virtual {v9, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_5b

    .line 48
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;

    move-result-object v9

    const/4 v10, 0x0

    aget-object v10, v8, v10

    .line 49
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v8, v11

    invoke-virtual {v9, v10, v11, v7}, Ljava/lang/invoke/MethodHandles$Lookup;->findStatic(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;

    move-result-object v9

    invoke-direct {v3, v9}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto :goto_39

    .line 50
    :cond_5b
    const-string v9, "super"

    invoke-virtual {v9, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_97

    .line 51
    invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;

    move-result-object v5

    .line 52
    .local v5, "lookup":Ljava/lang/invoke/MethodHandles$Lookup;
    const-class v9, Ljava/lang/invoke/MethodHandles$Lookup;

    const-string v10, "allowedModes"

    invoke-virtual {v9, v10}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v1

    .line 53
    .local v1, "allowedModes":Ljava/lang/reflect/Field;
    const/4 v9, 0x1

    invoke-virtual {v1, v9}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 54
    const/4 v9, -0x1

    invoke-static {v9}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v9

    invoke-virtual {v1, v5, v9}, Ljava/lang/reflect/Field;->set(Ljava/lang/Object;Ljava/lang/Object;)V

    .line 55
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v9, 0x0

    aget-object v9, v8, v9

    .line 56
    invoke-static {v9}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v9

    const/4 v10, 0x1

    aget-object v10, v8, v10

    const/4 v11, 0x0

    aget-object v11, v8, v11

    invoke-static {v11}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v11

    invoke-virtual {v5, v9, v10, v7, v11}, Ljava/lang/invoke/MethodHandles$Lookup;->findSpecial(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;

    move-result-object v9

    invoke-direct {v3, v9}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 57
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto :goto_39

    .end local v1    # "allowedModes":Ljava/lang/reflect/Field;
    .end local v5    # "lookup":Ljava/lang/invoke/MethodHandles$Lookup;
    :cond_97
    const-string v9, "constructor"

    invoke-virtual {v9, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_b5

    .line 58
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;

    move-result-object v9

    const/4 v10, 0x0

    aget-object v10, v8, v10

    .line 59
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    invoke-virtual {v9, v10, v7}, Ljava/lang/invoke/MethodHandles$Lookup;->findConstructor(Ljava/lang/Class;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;

    move-result-object v9

    invoke-direct {v3, v9}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto :goto_39

    .line 60
    :cond_b5
    const-string v9, "get"

    invoke-virtual {v9, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    :try_end_ba
    .catch Ljava/lang/Throwable; {:try_start_1 .. :try_end_ba} :catch_13f

    move-result v9

    if-eqz v9, :cond_fa

    .line 62
    :try_start_bd
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;

    move-result-object v9

    const/4 v10, 0x0

    aget-object v10, v8, v10

    .line 63
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v8, v11

    invoke-virtual {v7}, Ljava/lang/invoke/MethodType;->returnType()Ljava/lang/Class;

    move-result-object v12

    invoke-virtual {v9, v10, v11, v12}, Ljava/lang/invoke/MethodHandles$Lookup;->findGetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;

    move-result-object v9

    invoke-direct {v3, v9}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V
    :try_end_d8
    .catch Ljava/lang/Exception; {:try_start_bd .. :try_end_d8} :catch_db
    .catch Ljava/lang/Throwable; {:try_start_bd .. :try_end_d8} :catch_13f

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 67
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_39

    .line 64
    :catch_db
    move-exception v4

    .line 65
    .local v4, "e":Ljava/lang/Exception;
    :try_start_dc
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;

    move-result-object v9

    const/4 v10, 0x0

    aget-object v10, v8, v10

    .line 66
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v8, v11

    invoke-virtual {v7}, Ljava/lang/invoke/MethodType;->returnType()Ljava/lang/Class;

    move-result-object v12

    invoke-virtual {v9, v10, v11, v12}, Ljava/lang/invoke/MethodHandles$Lookup;->findStaticGetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;

    move-result-object v9

    invoke-direct {v3, v9}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 67
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_39

    .line 68
    .end local v4    # "e":Ljava/lang/Exception;
    :cond_fa
    const-string v9, "set"

    invoke-virtual {v9, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    :try_end_ff
    .catch Ljava/lang/Throwable; {:try_start_dc .. :try_end_ff} :catch_13f

    move-result v9

    if-eqz v9, :cond_39

    .line 70
    :try_start_102
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;

    move-result-object v9

    const/4 v10, 0x0

    aget-object v10, v8, v10

    .line 71
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v8, v11

    invoke-virtual {v7}, Ljava/lang/invoke/MethodType;->returnType()Ljava/lang/Class;

    move-result-object v12

    invoke-virtual {v9, v10, v11, v12}, Ljava/lang/invoke/MethodHandles$Lookup;->findSetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;

    move-result-object v9

    invoke-direct {v3, v9}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V
    :try_end_11d
    .catch Ljava/lang/Exception; {:try_start_102 .. :try_end_11d} :catch_120
    .catch Ljava/lang/Throwable; {:try_start_102 .. :try_end_11d} :catch_13f

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 75
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_39

    .line 72
    :catch_120
    move-exception v4

    .line 73
    .restart local v4    # "e":Ljava/lang/Exception;
    :try_start_121
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;

    move-result-object v9

    const/4 v10, 0x0

    aget-object v10, v8, v10

    .line 74
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v8, v11

    invoke-virtual {v7}, Ljava/lang/invoke/MethodType;->returnType()Ljava/lang/Class;

    move-result-object v12

    invoke-virtual {v9, v10, v11, v12}, Ljava/lang/invoke/MethodHandles$Lookup;->findStaticSetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;

    move-result-object v9

    invoke-direct {v3, v9}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V
    :try_end_13c
    .catch Ljava/lang/Throwable; {:try_start_121 .. :try_end_13c} :catch_13f

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_39

    .line 77
    .end local v0    # "action":Ljava/lang/String;
    .end local v4    # "e":Ljava/lang/Exception;
    .end local v7    # "type":Ljava/lang/invoke/MethodType;
    .end local v8    # "var":[Ljava/lang/String;
    :catch_13f
    move-exception v6

    .line 78
    .local v6, "throwable":Ljava/lang/Throwable;
    invoke-virtual {v6}, Ljava/lang/Throwable;->printStackTrace()V

    goto/16 :goto_39
.end method

.method public static getStrings()[Ljava/lang/String;
    .registers 1

    .prologue
    .line 103
    sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;

    return-object v0
.end method


# virtual methods
.method public varargs Toast([Ljava/lang/Object;)V
    .registers 4
    .param p1, "objects"    # [Ljava/lang/Object;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Throwable;
        }
    .end annotation

    .prologue
    const/4 v1, 0x1

    .line 22
    invoke-static {v1}, Lcall/InvokeDynamicTest;->callSite(I)Ljava/lang/invoke/CallSite;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/invoke/MethodHandle;->invoke()V

    .line 23
    invoke-static {v1}, Lcall/InvokeDynamicTest;->callSite(I)Ljava/lang/invoke/CallSite;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;

    move-result-object v0

    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Ljava/lang/invoke/MethodHandle;->invoke(Ljava/lang/Void;)V

    .line 24
    return-void
.end method

.method public ToastTest()V
    .registers 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Throwable;
        }
    .end annotation

    .prologue
    .line 19
    const/4 v0, 0x2

    new-array v0, v0, [Ljava/lang/Object;

    const/4 v1, 0x0

    aput-object p0, v0, v1

    const/4 v1, 0x1

    aput-object p0, v0, v1

    invoke-virtual {p0, v0}, Lcall/InvokeDynamicTest;->Toast([Ljava/lang/Object;)V

    .line 20
    return-void
.end method

.method public test()V
    .registers 6
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Throwable;
        }
    .end annotation

    .annotation runtime Lorg/junit/Test;
    .end annotation

    .prologue
    .line 85
    const/16 v2, 0xa

    new-array v2, v2, [Ljava/lang/String;

    iput-object v2, p0, Lcall/InvokeDynamicTest;->test:[Ljava/lang/String;

    .line 86
    const/16 v2, 0x14

    new-array v2, v2, [Ljava/lang/String;

    sput-object v2, Lcall/InvokeDynamicTest;->test2:[Ljava/lang/String;

    .line 87
    sget-object v3, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "test size "

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v4

    const/4 v2, 0x3

    invoke-static {v2}, Lcall/InvokeDynamicTest;->callSite(I)Ljava/lang/invoke/CallSite;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;

    move-result-object v2

    invoke-virtual {v2, p0}, Ljava/lang/invoke/MethodHandle;->invoke(Lcall/InvokeDynamicTest;)[Ljava/lang/String;

    move-result-object v2

    check-cast v2, [Ljava/lang/String;

    array-length v2, v2

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v3, v2}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 88
    const/4 v2, 0x4

    invoke-static {v2}, Lcall/InvokeDynamicTest;->callSite(I)Ljava/lang/invoke/CallSite;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;

    move-result-object v2

    const/16 v3, 0x64

    new-array v3, v3, [Ljava/lang/String;

    invoke-virtual {v2, p0, v3}, Ljava/lang/invoke/MethodHandle;->invoke(Lcall/InvokeDynamicTest;[Ljava/lang/String;)V

    .line 89
    sget-object v2, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "test size "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    iget-object v4, p0, Lcall/InvokeDynamicTest;->test:[Ljava/lang/String;

    array-length v4, v4

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 91
    sget-object v3, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "test2 size "

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v4

    const/4 v2, 0x5

    invoke-static {v2}, Lcall/InvokeDynamicTest;->callSite(I)Ljava/lang/invoke/CallSite;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/invoke/MethodHandle;->invoke()[Ljava/lang/String;

    move-result-object v2

    check-cast v2, [Ljava/lang/String;

    array-length v2, v2

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v3, v2}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 92
    const/4 v2, 0x6

    invoke-static {v2}, Lcall/InvokeDynamicTest;->callSite(I)Ljava/lang/invoke/CallSite;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;

    move-result-object v2

    const/16 v3, 0xc8

    new-array v3, v3, [Ljava/lang/String;

    invoke-virtual {v2, v3}, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/String;)V

    .line 93
    sget-object v2, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "test2 size "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    iget-object v4, p0, Lcall/InvokeDynamicTest;->test:[Ljava/lang/String;

    array-length v4, v4

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 95
    new-instance v1, Lcall/InvokeDynamicTest$testsuper;

    invoke-direct {v1, p0}, Lcall/InvokeDynamicTest$testsuper;-><init>(Lcall/InvokeDynamicTest;)V

    .line 96
    .local v1, "testsuper":Lcall/InvokeDynamicTest$testsuper;
    sget-object v2, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const/4 v3, 0x7

    invoke-static {v3}, Lcall/InvokeDynamicTest;->callSite(I)Ljava/lang/invoke/CallSite;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;

    move-result-object v3

    invoke-virtual {v3, v1}, Ljava/lang/invoke/MethodHandle;->invoke(Lcall/InvokeDynamicTest$testsuper;)Ljava/lang/Object;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V

    .line 98
    const/4 v2, 0x2

    invoke-static {v2}, Lcall/InvokeDynamicTest;->callSite(I)Ljava/lang/invoke/CallSite;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/invoke/MethodHandle;->invoke()Ljava/util/Date;

    move-result-object v0

    .line 99
    .local v0, "date":Ljava/util/Date;
    sget-object v2, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "Call Test ->"

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    const/4 v4, 0x1

    invoke-static {v4}, Lcall/InvokeDynamicTest;->callSite(I)Ljava/lang/invoke/CallSite;

    move-result-object v4

    invoke-virtual {v4}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;

    move-result-object v4

    invoke-virtual {v4, v0}, Ljava/lang/invoke/MethodHandle;->invoke(Ljava/util/Date;)Ljava/lang/Object;

    move-result-object v4

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 100
    return-void
.end method
