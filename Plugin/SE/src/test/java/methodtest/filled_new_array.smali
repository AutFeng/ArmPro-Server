.class public Lmethodtest/filled_new_array;
.super Ljava/lang/Object;
.source "filled_new_array.java"


# static fields
.field public static final ints:[I


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 4
    const/4 v0, 0x3

    new-array v0, v0, [I

    fill-array-data v0, :array_a

    sput-object v0, Lmethodtest/filled_new_array;->ints:[I

    return-void

    nop

    :array_a
    .array-data 4
        0x1
        0x2
        0x3
    .end array-data
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method
