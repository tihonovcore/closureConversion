var a = 777;
var b = 666;

function mom() {
    dad();
    a = a + 1;
}

function dad() {
    mom();
    b = b + 2;
}