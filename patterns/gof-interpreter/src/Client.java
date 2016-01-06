public class Client {
    public static void main(String[] args) {
        // ((a AND NOT b) OR c)

        LogicalExpression root = new LogicalOrOperator(
                new LogicalAndOperator(
                        new LogicalVariable("a"),
                        new LogicalNotOperator(
                                new LogicalVariable("b")
                        )
                ),
                new LogicalVariable("c")
        );

        System.out.println(root);

        Context context = new Context();
        context.setVariableToFalse("a");
        context.setVariableToTrue("b");
        context.setVariableToFalse("c");

        // = FALSE

        System.out.println(root.evaluate(context));
    }
}
