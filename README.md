# Evaluate expression
### Задание к стажировке

### Closure Conversion
###### 
[Converter](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/Converter.java "Converter")<br>
Вспомогательные классы: <br>
[FunctionDefinition](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/FunctionDefinition.java "FunctionDefinition")
Определяет функцию как списоки определенных переменных, используемых переменных, имён вызываемых методов и её текстового предствления<br>
[BuildTreeVisitor](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/BuildTreeVisitor.java "BTV")
Строит дерево зависимостей методов и находит изначальные замыкания<br>
[ConvertFunctionVisitor](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/ConvertFunctionVisitor.java "CFV")
Опираясь на результат работы **BuildTreeVisitor'a** преобразует все функции с замыканиями в функции верхнего уровная<br>


[Тесты](https://github.com/tihonovcore/closureConversion/tree/master/src/closureConversion/tests/scripts "Тесты")<br>
[TestVisitor](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/tests/TestVisitor.java "TestVisitor") 
Вспомогательный класс для тестов<br>
[ConverterTest](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/tests/ConverterTest.java "ConverterTest")
Проверяет наличие правильных вызовов и определений функций  