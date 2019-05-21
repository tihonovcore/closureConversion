# Evaluate expression
### Здание к стажировке
###### Основной класс:
[Converter](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/Converter.java "Converter")

###### Вспомогательные классы:
[FunctionDefinition](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/FunctionDefinition.java "FunctionDefinition")
Определяет функцию как список объявленных переменных, список используемых переменных, имён вызываемых методов и текстового предствления самой функции<br>
[BuildTreeVisitor](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/BuildTreeVisitor.java "BTV")
Строит дерево зависимостей методов и находит изначальные замыкания<br>
[ConvertFunctionVisitor](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/ConvertFunctionVisitor.java "CFV")
Опираясь на результат работы **BuildTreeVisitor'a** преобразует все функции с замыканиями в функции верхнего уровня<br>

###### Тестирование:
[Скрипт для запуска тестов](https://github.com/tihonovcore/closureConversion/blob/master/Tester.sh "Tester.sh") 
<code>./Tester.sh</code>, затем <code>build</code> или <code>test</code><br>
[Папка с тестами](https://github.com/tihonovcore/closureConversion/tree/master/src/closureConversion/tests/scripts "Тесты")<br>
[TestVisitor](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/tests/TestVisitor.java "TestVisitor") 
Вспомогательный класс для тестов<br>
[ConverterTest](https://github.com/tihonovcore/closureConversion/blob/master/src/closureConversion/tests/ConverterTest.java "ConverterTest")
Проверяет наличие правильных вызовов и определений функций  
