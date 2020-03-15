java_binary(
    name = "Buggeroo",
    srcs = glob(["src/main/java/com/nealsid/buggeroo/*.java"]),
    deps = [
        "@maven//:net_sourceforge_argparse4j_argparse4j"
    ],
    main_class = "com.nealsid.buggeroo.Main"
)

java_binary(
    name = "DbgFibonacci",
    runtime_deps = [
        ":Buggeroo",
        ":Fibonacci",
        ":libFibonacci-src.jar",
        "@maven//:net_sourceforge_argparse4j_argparse4j"
    ],
    args = [
        "com.nealsid.buggeroo.Fibonacci",
        "--targetcp $(BINDIR)/libFibonacci.jar",
        "--srcjarcp $(BINDIR)/libFibonacci-src.jar"
    ],
    main_class = "com.nealsid.buggeroo.Main"
)

java_library(
    name = "Fibonacci",
    srcs = ["src/main/java/com/nealsid/buggeroo/Fibonacci.java"]
)
