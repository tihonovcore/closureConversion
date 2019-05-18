function fun() {
    return B();
}


var a = 10 + 9 + 8;

function B() {
    var b = a + 5;

    function C() {
        var c = b;

        function D() {
            var d = b + c;
        }
    }
}
