.class public Lcall/InvokeDynamicTest$testsuper;
.super Lcall/InvokeDynamicTest$testsuper2;
.source "InvokeDynamicTest.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcall/InvokeDynamicTest;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x1
    name = "testsuper"
.end annotation


# instance fields
.field final synthetic this$0:Lcall/InvokeDynamicTest;


# direct methods
.method public constructor <init>(Lcall/InvokeDynamicTest;)V
    .registers 2
    .param p1, "this$0"    # Lcall/InvokeDynamicTest;

    .prologue
    .line 106
    iput-object p1, p0, Lcall/InvokeDynamicTest$testsuper;->this$0:Lcall/InvokeDynamicTest;

    invoke-direct {p0, p1}, Lcall/InvokeDynamicTest$testsuper2;-><init>(Lcall/InvokeDynamicTest;)V

    return-void
.end method


# virtual methods
.method public test()I
    .registers 2

    .prologue
    .line 108
    invoke-super {p0}, Lcall/InvokeDynamicTest$testsuper2;->test()I

    move-result v0

    return v0
.end method
