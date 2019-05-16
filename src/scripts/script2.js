var t = "p";

var fun1 = function () {
    var f = function () {
        print('asd');
    };

    f = 21 + square(t);

    return f + 15;
};

t = 23456789;

var square = function(value) {
    return value + value;
};

var fun2 = function (object) {
    print(Object.prototype.toString.call(object) + "STRING");
};