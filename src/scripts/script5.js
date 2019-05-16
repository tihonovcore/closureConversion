var getValue = function(a, b, c, d) {
    return a + b + c + d;
};

var fun = function () {
    var temp = getValue(1, 2, 3, 4) + getValue(-1, -2, -3, -4);
};

var ultraFun = function () {
    var s = getValue(1, getValue(4, 5, 6, 7), 4, getValue(1, 2, 3, 4) + getValue(4, 3, 2, 1));
};