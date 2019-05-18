function A() {
    var a = 5;

    function B() {
        var b = a;

        function C() {
            var c = a + b;
            return c;
        }
        return b;
    }
    return a;
}
