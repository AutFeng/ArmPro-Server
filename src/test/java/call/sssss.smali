.class public Lcall/sssss;
.super Ljava/lang/Object;
.source "sssss.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 6
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static aaa(Lcall/MethodSeparateTest;)Ljava/lang/String;
    .registers 8
    .param p0, "test"    # Lcall/MethodSeparateTest;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Exception;
        }
    .end annotation

    .prologue
    const/4 v6, 0x0

    const/4 v5, 0x1

    .line 16
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v1

    const-string v2, "name"

    new-array v3, v5, [Ljava/lang/Class;

    sget-object v4, Ljava/lang/Integer;->TYPE:Ljava/lang/Class;

    aput-object v4, v3, v6

    invoke-virtual {v1, v2, v3}, Ljava/lang/Class;->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object v0

    .line 17
    .local v0, "method":Ljava/lang/reflect/Method;
    invoke-virtual {v0}, Ljava/lang/reflect/Method;->getModifiers()I

    move-result v1

    invoke-static {v1}, Ljava/lang/reflect/Modifier;->isPublic(I)Z

    move-result v1

    if-eqz v1, :cond_21

    .line 18
    invoke-virtual {p0}, Lcall/MethodSeparateTest;->test4()Ljava/lang/String;

    move-result-object v1

    .line 21
    :goto_20
    return-object v1

    .line 20
    :cond_21
    invoke-virtual {v0, v5}, Ljava/lang/reflect/Method;->setAccessible(Z)V

    .line 21
    new-array v1, v5, [Ljava/lang/Object;

    const/16 v2, 0x64

    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v2

    aput-object v2, v1, v6

    invoke-virtual {v0, p0, v1}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/lang/String;

    goto :goto_20
.end method

.method private static s(Lcall/MethodSeparateTest;)V
    .registers 1
    .param p0, "separateTest"    # Lcall/MethodSeparateTest;

    .prologue
    .line 8
    invoke-virtual {p0}, Lcall/MethodSeparateTest;->test()V

    .line 10
    invoke-static {}, Lcall/MethodSeparateTest;->test3()V

    .line 12
    return-void
.end method
