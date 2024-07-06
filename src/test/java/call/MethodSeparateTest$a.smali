.class Lcall/MethodSeparateTest$a;
.super Ljava/lang/Object;
.source "MethodSeparateTest.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcall/MethodSeparateTest;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "a"
.end annotation


# instance fields
.field final synthetic this$0:Lcall/MethodSeparateTest;


# direct methods
.method private constructor <init>(Lcall/MethodSeparateTest;)V
    .registers 2

    .prologue
    .line 17
    iput-object p1, p0, Lcall/MethodSeparateTest$a;->this$0:Lcall/MethodSeparateTest;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method synthetic constructor <init>(Lcall/MethodSeparateTest;Lcall/MethodSeparateTest$1;)V
    .registers 3
    .param p1, "x0"    # Lcall/MethodSeparateTest;
    .param p2, "x1"    # Lcall/MethodSeparateTest$1;

    .prologue
    .line 17
    invoke-direct {p0, p1}, Lcall/MethodSeparateTest$a;-><init>(Lcall/MethodSeparateTest;)V

    return-void
.end method
