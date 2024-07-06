.class public Lcall/MethodSeparateTest$aaa;
.super Ljava/lang/Object;
.source "MethodSeparateTest.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcall/MethodSeparateTest;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x9
    name = "aaa"
.end annotation


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 16
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method private s(Lcall/MethodSeparateTest;)V
    .registers 2
    .param p1, "separateTest"    # Lcall/MethodSeparateTest;

    .prologue
    .line 18
    invoke-virtual {p1}, Lcall/MethodSeparateTest;->test()V

    .line 19
    return-void
.end method
