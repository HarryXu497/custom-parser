public class Directive {
    private final DirectiveType type;
    private final int charNumber;

    public Directive(DirectiveType type, int charNumber) {
        this.type = type;
        this.charNumber = charNumber;
    }

    public DirectiveType getType() {
        return this.type;
    }

    public int getCharNumber() {
        return this.charNumber;
    }
}
