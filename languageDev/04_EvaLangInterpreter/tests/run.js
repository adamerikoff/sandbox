const { Eva } = require('../src/Eva');
const { Environment } = require('../src/Environment');
const Parser = require('../parser/evaParser');

const tests = [
    require('./self-eval-test.js'),
    require('./math-test.js'),
    require('./variables-test.js'),
    require('./block-test.js'),
    require('./if-test.js'),
    require('./while-test.js'),
    require('./built-in-function-test.js'),
    require('./user-defined-function-test'),
    require('./lambda-function-test'),
    require('./switch-test'),
    require('./for-test'),
    require('./inc-test'),
    require('./class-test'),
    require('./module-test'),
    require('./import-test'),
];

// ---------------------------------------------------------------------
// Tests:
const eva = new Eva();

// that 'test' has no relationship with 'test()' function in test-util.js
tests.forEach(test => test(eva)); // forEach syntax: treat each element as test then run its export 
eva.eval(['print', '"Hello, "', '"World!"']);
eva.eval(['var', 'w', -5]);
eva.eval(Parser.parse(`(print "Hello,  World!")`));

// module.exports = name => (name) then excute the statements inside the {}
console.log('All assertions passed!');
