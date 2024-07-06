.class public Lxml/ssss;
.super Ljava/lang/Object;
.source "ssss.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 5
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static get(Lxml/TestRefle;)Ljava/lang/String;
    .registers 5
    .param p0, "refle"    # Lxml/TestRefle;

    .prologue
    .line 21
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v0

    .line 24
    .local v0, "cls":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    :goto_4
    :try_start_4
    const-string v3, "test"

    invoke-virtual {v0, v3}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v1

    .line 25
    .local v1, "declaredField":Ljava/lang/reflect/Field;
    const/4 v3, 0x1

    invoke-virtual {v1, v3}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 26
    invoke-virtual {v1, p0}, Ljava/lang/reflect/Field;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;
    :try_end_14
    .catch Ljava/lang/Exception; {:try_start_4 .. :try_end_14} :catch_15

    return-object v3

    .line 27
    .end local v1    # "declaredField":Ljava/lang/reflect/Field;
    :catch_15
    move-exception v2

    .line 28
    .local v2, "e":Ljava/lang/Exception;
    invoke-virtual {v0}, Ljava/lang/Class;->getSuperclass()Ljava/lang/Class;

    move-result-object v0

    .line 29
    goto :goto_4
.end method

.method public static set(Lxml/TestRefle;)V
    .registers 5
    .param p0, "refle"    # Lxml/TestRefle;

    .prologue
    .line 7
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v0

    .line 10
    .local v0, "cls":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    :goto_4
    :try_start_4
    const-string v3, "test"

    invoke-virtual {v0, v3}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v1

    .line 11
    .local v1, "declaredField":Ljava/lang/reflect/Field;
    const/4 v3, 0x1

    invoke-virtual {v1, v3}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 12
    const/4 v3, 0x0

    invoke-virtual {v1, v3, p0}, Ljava/lang/reflect/Field;->set(Ljava/lang/Object;Ljava/lang/Object;)V
    :try_end_12
    .catch Ljava/lang/Exception; {:try_start_4 .. :try_end_12} :catch_13

    .line 13
    return-void

    .line 14
    .end local v1    # "declaredField":Ljava/lang/reflect/Field;
    :catch_13
    move-exception v2

    .line 15
    .local v2, "e":Ljava/lang/Exception;
    invoke-virtual {v0}, Ljava/lang/Class;->getSuperclass()Ljava/lang/Class;

    move-result-object v0

    .line 16
    goto :goto_4
.end method
