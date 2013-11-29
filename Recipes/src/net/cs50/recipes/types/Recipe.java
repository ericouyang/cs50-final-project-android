package net.cs50.recipes.types;

public class Recipe {
    public final int id;
    public final String name;
    public final long createdAt;
    public final long modifiedAt;

    public Recipe(int id, String name, long createdAt, long modifiedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}
