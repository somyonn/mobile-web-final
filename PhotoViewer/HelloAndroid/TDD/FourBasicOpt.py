class FourBasicOpt:
    """
    For the four basic arithmetic operations.
    Class can be used to add(addition), subtract(subtraction), divide(division) and multiply(multiplication).
    """
    def add(self, x, y):
        return x + y

    def subtract(self, x, y):
        return x - y

    def divide(self, x, y):
        if y == 0:
            return 0
        return x / y

    def multiply(self, x, y):
        return x * y
