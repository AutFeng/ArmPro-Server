.class public Lcall/InvokeDynamicTest$testsuper2;
.super Ljava/lang/Object;
.source "InvokeDynamicTest.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcall/InvokeDynamicTest;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x1
    name = "testsuper2"
.end annotation


# instance fields
.field final synthetic this$0:Lcall/InvokeDynamicTest;


# direct methods
.method public constructor <init>(Lcall/InvokeDynamicTest;)V
    .registers 2
    .param p1, "this$0"    # Lcall/InvokeDynamicTest;

    .prologue
    .line 112
    iput-object p1, p0, Lcall/InvokeDynamicTest$testsuper2;->this$0:Lcall/InvokeDynamicTest;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public test()I
    .registers 2

    .prologue
    .line 115
    const v0, 0x3da8d3e

    return v0
.end method
