function bar(a, b, c) {
    return a + b + c;
};

function foo(a) {
    var b = 42;
    return bar(a, b, 24);
};
