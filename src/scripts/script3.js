var fun1 = function () {
    var a = function () {
        print('a');
    };

    var ba = function () {
        print('b' + a());
    }
};

var fun2 = function (object) {
    var one = 1;
    var two = 2;

    var tree = function () {
        return one + two;
    }
};