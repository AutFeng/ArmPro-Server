.class public Lcall/MethodSeparateTest;
.super Ljava/lang/Object;
.source "MethodSeparateTest.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcall/MethodSeparateTest$a;
    }
.end annotation


# static fields
.field private static s:Ljava/lang/String;

.field protected static s1:Ljava/lang/String;


# instance fields
.field private s2:Ljava/lang/String;

.field protected s3:Ljava/lang/String;


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 5
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method private test1()V
    .registers 1

    .prologue
    .line 26
    return-void
.end method

.method private static test2()V
    .registers 0

    .prologue
    .line 30
    return-void
.end method

.method protected static test3()V
    .registers 0

    .prologue
    .line 34
    return-void
.end method


# virtual methods
.method public s(Ljava/lang/Object;)V
    .registers 10
    .param p1, "object"    # Ljava/lang/Object;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Exception;
        }
    .end annotation

    .prologue
    const/4 v7, 0x0

    const/4 v6, 0x1

    .line 11
    invoke-virtual {p1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v2

    invoke-static {v2}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v2

    const-string v3, "test"

    new-array v4, v6, [Ljava/lang/Class;

    sget-object v5, Ljava/lang/Integer;->TYPE:Ljava/lang/Class;

    aput-object v5, v4, v7

    invoke-virtual {v2, v3, v4}, Ljava/lang/Class;->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object v0

    .line 12
    .local v0, "method":Ljava/lang/reflect/Method;
    invoke-virtual {v0, v6}, Ljava/lang/reflect/Method;->setAccessible(Z)V

    .line 13
    new-array v2, v6, [Ljava/lang/Object;

    invoke-static {v6}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v3

    aput-object v3, v2, v7

    invoke-virtual {v0, p1, v2}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;

    .line 14
    new-instance v1, Lcall/MethodSeparateTest$a;

    const/4 v2, 0x0

    invoke-direct {v1, p0, v2}, Lcall/MethodSeparateTest$a;-><init>(Lcall/MethodSeparateTest;Lcall/MethodSeparateTest$1;)V

    .line 15
    .local v1, "ss":Lcall/MethodSeparateTest$a;
    return-void
.end method

.method protected test()V
    .registers 1

    .prologue
    .line 22
    return-void
.end method
