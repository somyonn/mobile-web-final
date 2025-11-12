# test_FourBasicOpt.py
import unittest
from FourBasicOpt import FourBasicOpt

class FourBasicOptTest(unittest.TestCase):
    def test_add_01(self):
        four_opt = FourBasicOpt()
        self.assertEqual(four_opt.add(100, 10), 110)

    def test_add_02(self):
        four_opt = FourBasicOpt()
        self.assertEqual(four_opt.add(100, -90), 10)

    def test_subtract_01(self):
        four_opt = FourBasicOpt()
        self.assertEqual(four_opt.subtract(100, 10), 90)

    def test_subtract_02(self):
        four_opt = FourBasicOpt()
        self.assertEqual(four_opt.subtract(100, 110), -10)

    def test_divide_01(self):
        four_opt = FourBasicOpt()
        self.assertEqual(four_opt.divide(100, 10), 10)

    def test_divide_02(self):
        four_opt = FourBasicOpt()
        self.assertEqual(four_opt.divide(100, 0), 0)

    def test_multiply_01(self):
        four_opt = FourBasicOpt()
        self.assertEqual(four_opt.multiply(100, 1), 100)

    def test_multiply_02(self):
        four_opt = FourBasicOpt()
        self.assertEqual(four_opt.multiply(100, 10), 1000)


if __name__ == "__main__":
    unittest.main()
