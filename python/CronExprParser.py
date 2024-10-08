from datetime import datetime, timedelta
import functools
import unittest


class ChronoUtils:
    _FIELD_RANKS = ('microsecond', 'second', 'minute', 'hour', 'day', 'month', 'year')

    _MONTH_DAYS_COUNT = (0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    _FIELD_RANGES = {
        'month': (1, 12), 'day': (1, 31), 'weekday': (0, 6), 
        'hour': (0, 23), 'minute': (0, 59), 'second': (0, 59),
        'microsecond': (0, 999999),
    }

    @staticmethod
    def _is_leap_year(year):
        if year % 1000 == 0: return True
        if year % 100 == 0: return False
        if year % 4 == 0: return True
        return False
    
    @classmethod
    def field(cls, rank):
        return cls._FIELD_RANKS[rank]

    @classmethod
    def field_rank(cls, field):
        if field not in cls._FIELD_RANKS:
            return None
        return cls._FIELD_RANKS.index(field)

    @classmethod
    def lower_ranked_fields(cls, rank):
        return cls._FIELD_RANKS[:rank]
    
    @classmethod
    def higher_ranked_fields(cls, rank):
        return cls._FIELD_RANKS[rank+1:]

    @classmethod
    def field_range(cls, field):
        return cls._FIELD_RANGES[field]
    
    @classmethod
    def add_timedelta(cls, dt: datetime, field, delta):
        if field == 'month':
            month = getattr(dt, field) + delta
            year = dt.year + month // cls.field_range(field)[1]
            month = month % cls.field_range(field)[1]
            kwargs = {'month': month, 'year': year}
            return dt.replace(**kwargs)
        elif field == 'year':
            return dt.replace(year=dt.year + delta)
        
        kwargs = {field + 's': delta}
        return dt + timedelta(**kwargs)
    
    @classmethod
    def month_days(cls, year, month):
        if month == 2:
            return 29 if cls._is_leap_year(year) else 28
        return cls._MONTH_DAYS_COUNT[month]
        

class CronFieldType:

    FIELD = None

    @property
    def range(self):
        return ChronoUtils.field_range(self.FIELD)

    def get(self, dt: datetime):
        return getattr(dt, self.FIELD)
    
    def round_up(self, dt: datetime):
        rank = ChronoUtils.field_rank(self.FIELD)
        return self.reset(ChronoUtils.add_timedelta(dt, ChronoUtils.field(rank+1), 1))
    
    def elapse_to(self, dt: datetime, next_: int):
        if next_ == self.get(dt):
            return dt
        if next_ < self.get(dt):
            dt = self.round_up(dt)
        while self.FIELD == 'day' and next_ > ChronoUtils.month_days(dt.year, dt.month):
            dt = self.round_up(dt)

        kwargs = { self.FIELD: next_ }
        return self._reset_lower_ranked(dt, self.FIELD, **kwargs)
    
    def reset(self, dt, **extra):
        kwargs = {self.FIELD: self.range[0], **extra}
        return self._reset_lower_ranked(dt, self.FIELD, **kwargs)
    
    def _reset_lower_ranked(self, dt, field, **extra):
        day_rank = ChronoUtils.field_rank(field)
        kwargs = {field: ChronoUtils.field_range(field)[0] 
                  for field in ChronoUtils.lower_ranked_fields(day_rank)}
        return dt.replace(**kwargs, **extra)


class SecondCronFieldType(CronFieldType):
    FIELD = 'second'


class MinuteCronFieldType(CronFieldType):
    FIELD = 'minute'


class HourCronFieldType(CronFieldType):
    FIELD = 'hour'


class DayOfMonthCronFieldType(CronFieldType):
    FIELD = 'day'
    

class MonthCronFieldType(CronFieldType):
    FIELD = 'month'


class DayOfWeekCronFieldType(CronFieldType):
    FIELD = 'weekday'

    def get(self, dt: datetime):
        return dt.weekday()
    
    def round_up(self, dt: datetime):
        return self.elapse_to(dt, self.range[0])
    
    def elapse_to(self, dt: datetime, next_: int):
        days_a_week = self.range[1] - self.range[0] + 1
        delta = (next_ - self.get(dt) + days_a_week) % days_a_week

        if delta <= 0: return dt
        new_dt = ChronoUtils.add_timedelta(dt, 'day', delta)
        return self._reset_lower_ranked(new_dt, 'day')
    
    def reset(self, dt, **extra):
        new_dt = ChronoUtils.add_timedelta(dt, 'day', -self.get(dt))
        return self._reset_lower_ranked(new_dt, 'day', **extra)


class CronField:

    ATTEMPTS = 2
    MASK = 0x3FFFFFFFFFFFFFFF
    MONTHS = ('JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 
              'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC')
    DAYS_OF_WEEK = ('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN')

    def __init__(self, expr, type_):
        self._bits = 0
        self.expr = expr
        self.type = type_
    
    def _set_bits(self, lower, upper, delta):
        bits = functools.reduce(
            lambda t, s: t | (1 << s), 
            range(lower, upper + 1, delta),
            1 << lower
        )
        self._bits = (self._bits | bits) & self.MASK

    def _set_bit(self, index):
        range_datetime_min_val, range_max = self.type.range
        if range_datetime_min_val <= index <= range_max:
            self._bits = (self._bits | 1 << index) & self.MASK

    def _invalid_expr(self):
        raise ValueError('invalid expr \'' + self.expr + '\'')
    
    def _next_set_bit(self, from_index):
        masked = self._bits & (self.MASK << from_index)
        x = masked & -masked
        if x <= 0: return -1
        n = 0
        if x > (1 << 32): n += 32; x >>= 32
        if x > (1 << 16): n += 16; x >>= 16
        if x > (1 << 8):  n += 8;  x >>= 8
        if x > (1 << 4):  n += 4;  x >>= 4
        if x > (1 << 2):  n += 2;  x >>= 2
        return n + (x >> 1)
    
    def next_or_same(self, dt):
        current = self.type.get(dt)
        next_ = self._next_set_bit(current)
        if next_ == -1:
            dt = self.type.round_up(dt)
            current = 0
            next_ = self._next_set_bit(0)
        
        count = 0
        while next_ != current and count < self.ATTEMPTS:
            dt = self.type.elapse_to(dt, next_)
            current = self.type.get(dt)
            next_ = self._next_set_bit(current)
            if next_ == -1:
                dt = self.type.round_up(dt)
                next_ = self._next_set_bit(0)

            count += 1

        return dt if next_ == current else None
    
    @staticmethod
    def _replace_names(expr: str, from_, to_):
        for old, new in zip(from_, to_):
            expr = expr.replace(old, new)
        return expr
    
    @classmethod
    def _parse_field(cls, expr: str, type_: CronFieldType):
        result = cls(expr, type_)
        if expr == None:
            return result._invalid_expr()
        
        for field in expr.split(','):
            field_len = len(field)
            if field_len == 0:
                return result._invalid_expr()

            slash_pos = field.find('/')
            if slash_pos == 0 or slash_pos == field_len - 1:
                return result._invalid_expr()
            elif slash_pos != -1:
                delta = int(field[slash_pos+1:])
                cls._parse_range(result, field[:slash_pos], delta)
            else:
                cls._parse_range(result, field)
        return result
    
    @classmethod
    def _parse_range(cls, field: 'CronField', expr: str, delta = 1):
        expr_len = len(expr)
        if expr == None or expr_len == 0:
            return field._invalid_expr()
        range_datetime_min_val, range_max = field.type.range

        if expr == '*':
            field._set_bits(range_datetime_min_val, range_max, delta)
            return

        dash_pos = expr.find('-')
        if dash_pos == 0 or dash_pos == expr_len - 1:
            return field._invalid_expr()
        elif dash_pos != -1:
            lower, upper = int(expr[:dash_pos]), int(expr[dash_pos+1:])
            if not (range_datetime_min_val <= lower < upper <= range_max): 
                return field._invalid_expr()
            field._set_bits(lower, upper, delta)
        else:
            index = int(expr)
            if range_datetime_min_val <= index <= range_max:
                field._set_bit(index)
            else: return field._invalid_expr()

    @classmethod
    def parse_seconds(cls, expr):
        return cls._parse_field(expr, SecondCronFieldType())

    @classmethod
    def parse_minutes(cls, expr):
        return cls._parse_field(expr, MinuteCronFieldType())

    @classmethod
    def parse_hours(cls, expr):
        return cls._parse_field(expr, HourCronFieldType())

    @classmethod
    def parse_days_of_month(cls, expr):
        if expr == '?':
            return cls._parse_field('*', DayOfMonthCronFieldType())
        return cls._parse_field(expr, DayOfMonthCronFieldType())

    @classmethod
    def parse_months(cls, expr):
        type_ = MonthCronFieldType()
        month_nums = (str(m) for m in range(type_.range[0], type_.range[1] + 1))
        cls._replace_names(expr, cls.MONTHS, month_nums)
        return cls._parse_field(expr, type_)

    @classmethod
    def parse_days_of_week(cls, expr):
        type_ = DayOfWeekCronFieldType()
        weekday_nums = (str(m) for m in range(type_.range[0], type_.range[1] + 1))
        cls._replace_names(expr, cls.DAYS_OF_WEEK, weekday_nums)
        if expr == '?':
            return cls._parse_field('*', type_)
        return cls._parse_field(expr, type_)
    

class CronExpression:
    ATTEMPTS = 366
    FIELD_COUNT = 5
    ZERO_SECONDS_CRON_FIELD = CronField.parse_seconds('0')

    def __init__(self, expression):
        self.expression = expression
        self.fields = self._parse_fields(expression)
    
    def next(self, dt = None):
        dt = dt or datetime.now()
        return self._next_or_same(dt + timedelta(seconds=1))

    def _parse_fields(self, expression):
        fields = expression.split()
        if len(fields) != self.FIELD_COUNT:
            raise ValueError('invalid expression ' + expression)
        minutes = CronField.parse_minutes(fields[0])
        hours = CronField.parse_hours(fields[1])
        days_of_month = CronField.parse_days_of_month(fields[2])
        months = CronField.parse_months(fields[3])
        days_of_week = CronField.parse_days_of_week(fields[4])
        return (months, days_of_week, days_of_month, 
                hours, minutes, self.ZERO_SECONDS_CRON_FIELD)

    def _next_or_same(self, dt):
        for _ in range(self.ATTEMPTS):
            result = dt
            for field in self.fields:
                result = field.next_or_same(result)
            if result == None or result == dt:
                return result
            dt = result
        return None

    @classmethod
    def parse(cls, expression):
        if expression is None:
            raise ValueError('invalid expression(None) ')
        return cls(expression)


class CronFieldTypeTest(unittest.TestCase):
    def test_second(self):
        cft = SecondCronFieldType()
        dt = datetime.fromisoformat('2024-07-01 15:30:12')
        self.assertEqual(cft.get(dt), 12)
        self.assertEqual(cft.reset(dt), datetime.fromisoformat('2024-07-01 15:30:00'))
        self.assertEqual(cft.elapse_to(dt, 25), datetime.fromisoformat('2024-07-01 15:30:25'))
        self.assertEqual(cft.elapse_to(dt, 5), datetime.fromisoformat('2024-07-01 15:31:05'))
        self.assertEqual(cft.round_up(dt), datetime.fromisoformat('2024-07-01 15:31:00'))
    
    def test_minute(self):
        cft = MinuteCronFieldType()
        dt = datetime.fromisoformat('2024-07-01 15:31:12')
        self.assertEqual(cft.get(dt), 31)
        self.assertEqual(cft.reset(dt), datetime.fromisoformat('2024-07-01 15:00:00'))
        self.assertEqual(cft.elapse_to(dt, 35), datetime.fromisoformat('2024-07-01 15:35:00'))
        self.assertEqual(cft.elapse_to(dt, 15), datetime.fromisoformat('2024-07-01 16:15:00'))
        self.assertEqual(cft.round_up(dt), datetime.fromisoformat('2024-07-01 16:00:00'))

    def test_hour(self):
        cft = HourCronFieldType()
        dt = datetime.fromisoformat('2024-07-01 15:31:12')
        self.assertEqual(cft.get(dt), 15)
        self.assertEqual(cft.reset(dt), datetime.fromisoformat('2024-07-01 00:00:00'))
        self.assertEqual(cft.elapse_to(dt, 18), datetime.fromisoformat('2024-07-01 18:00:00'))
        self.assertEqual(cft.elapse_to(dt, 11), datetime.fromisoformat('2024-07-02 11:00:00'))
        self.assertEqual(cft.round_up(dt), datetime.fromisoformat('2024-07-02 00:00:00'))

    def test_day_of_month(self):
        cft = DayOfMonthCronFieldType()
        dt = datetime.fromisoformat('2024-07-05 15:31:12')
        self.assertEqual(cft.get(dt), 5)
        self.assertEqual(cft.reset(dt), datetime.fromisoformat('2024-07-01 00:00:00'))
        self.assertEqual(cft.elapse_to(dt, 18), datetime.fromisoformat('2024-07-18 00:00:00'))
        self.assertEqual(cft.elapse_to(dt, 3), datetime.fromisoformat('2024-08-03 00:00:00'))
        self.assertEqual(cft.round_up(dt), datetime.fromisoformat('2024-08-01 00:00:00'))

    def test_month(self):
        cft = MonthCronFieldType()
        dt = datetime.fromisoformat('2024-07-05 15:31:12')
        # self.assertEqual(cft.get(dt), 7)
        # self.assertEqual(cft.reset(dt), datetime.fromisoformat('2024-01-01 00:00:00'))
        # self.assertEqual(cft.elapse_to(dt, 11), datetime.fromisoformat('2024-11-01 00:00:00'))
        self.assertEqual(cft.elapse_to(dt, 3), datetime.fromisoformat('2025-03-01 00:00:00'))
        self.assertEqual(cft.round_up(dt), datetime.fromisoformat('2025-01-01 00:00:00'))

    def test_day_of_week(self):
        cft = DayOfWeekCronFieldType()
        dt = datetime.fromisoformat('2024-06-06 15:31:12')
        self.assertEqual(cft.get(dt), 3)
        self.assertEqual(cft.reset(dt), datetime.fromisoformat('2024-06-03 00:00:00'))
        self.assertEqual(cft.elapse_to(dt, 6), datetime.fromisoformat('2024-06-09 00:00:00'))
        self.assertEqual(cft.elapse_to(dt, 2), datetime.fromisoformat('2024-06-12 00:00:00'))
        self.assertEqual(cft.round_up(dt), datetime.fromisoformat('2024-06-10 00:00:00'))


class CronFieldTests(unittest.TestCase):

    def _test_field(self, field, last, expected):
        last_dt = datetime.fromisoformat(last)
        expected_dt = datetime.fromisoformat(expected)
        dt = field.next_or_same(last_dt)
        self.assertIsNotNone(dt)
        self.assertEqual(dt, expected_dt)

    def test_minutes(self):
        field = CronField.parse_minutes('*')
        self._test_field(field, '2024-07-01 15:30:00', '2024-07-01 15:30:00')

        field = CronField.parse_minutes('*/4')
        self._test_field(field, '2024-07-01 15:30:12', '2024-07-01 15:32:00')

        field = CronField.parse_minutes('10-15')
        self._test_field(field, '2024-07-01 15:59:32', '2024-07-01 16:10:00')

        field = CronField.parse_minutes('11-23/5')
        self._test_field(field, '2024-07-01 15:17:18', '2024-07-01 15:21:00')

        field = CronField.parse_minutes('11,17,19')
        self._test_field(field, '2024-07-01 15:38:11', '2024-07-01 16:11:00')

        field = CronField.parse_minutes('1')
        self._test_field(field, '2024-12-31 23:59:59', '2025-01-01 00:01:00')

        self.assertRaises(ValueError, lambda: CronField.parse_minutes('60'))
        self.assertRaises(ValueError, lambda: CronField.parse_minutes('1/'))
        self.assertRaises(ValueError, lambda: CronField.parse_minutes('/2'))
        self.assertRaises(ValueError, lambda: CronField.parse_minutes('1-'))
        self.assertRaises(ValueError, lambda: CronField.parse_minutes('-2'))
        self.assertRaises(ValueError, lambda: CronField.parse_minutes('1,'))

    def test_hours(self):
        field = CronField.parse_hours('*')
        self._test_field(field, '2024-07-01 15:30:00', '2024-07-01 15:30:00')

        field = CronField.parse_hours('1')
        self._test_field(field, '2024-12-31 23:59:59', '2025-01-01 01:00:00')

        self.assertRaises(ValueError, lambda: CronField.parse_hours('24'))

    def test_days_of_month(self):
        field = CronField.parse_days_of_month('*')
        self._test_field(field, '2024-07-01 15:30:00', '2024-07-01 15:30:00')

        field = CronField.parse_days_of_month('?')
        self._test_field(field, '2024-07-01 15:30:00', '2024-07-01 15:30:00')

        field = CronField.parse_days_of_month('5')
        self._test_field(field, '2024-12-31 23:59:59', '2025-01-05 00:00:00')

        field = CronField.parse_days_of_month('31')
        self._test_field(field, '2024-06-21 15:30:15', '2024-07-31 00:00:00')

        self.assertRaises(ValueError, lambda: CronField.parse_days_of_month('0'))
        self.assertRaises(ValueError, lambda: CronField.parse_days_of_month('32'))

    def test_months(self):
        field = CronField.parse_months('*')
        self._test_field(field, '2024-07-01 15:30:00', '2024-07-01 15:30:00')

        field = CronField.parse_months('1')
        self._test_field(field, '2024-07-21 15:30:00', '2025-01-01 00:00:00')

        field = CronField.parse_months('5')
        self._test_field(field, '2024-07-21 15:30:00', '2025-05-01 00:00:00')

        self.assertRaises(ValueError, lambda: CronField.parse_months('0'))
        self.assertRaises(ValueError, lambda: CronField.parse_months('13'))

    def test_days_of_week(self):
        field = CronField.parse_days_of_week('*')
        self._test_field(field, '2024-07-01 15:30:00', '2024-07-01 15:30:00')

        field = CronField.parse_days_of_week('?')
        self._test_field(field, '2024-07-01 15:30:00', '2024-07-01 15:30:00')

        field = CronField.parse_days_of_week('3')
        self._test_field(field, '2024-07-01 15:30:00', '2024-07-04 00:00:00')

        field = CronField.parse_days_of_week('0')
        self._test_field(field, '2024-07-02 15:30:00', '2024-07-08 00:00:00')

        field = CronField.parse_days_of_week('0')
        self._test_field(field, '2024-12-31 15:30:00', '2025-01-06 00:00:00')

        self.assertRaises(ValueError, lambda: CronField.parse_days_of_week('7'))


class CronExpressionTests(unittest.TestCase):

    def _test_expression(self, expr, last, expected):
        if not isinstance(last, datetime):
            last = datetime.fromisoformat(last)
        expected_dt = datetime.fromisoformat(expected)
        next_dt = expr.next(last)
        self.assertEqual(next_dt, expected_dt)
        return next_dt

    def test_invalid(self):
        self.assertRaises(ValueError, lambda: CronExpression.parse(None))
        self.assertRaises(ValueError, lambda: CronExpression.parse(''))
        self.assertRaises(ValueError, lambda: CronExpression.parse('*'))
        self.assertRaises(ValueError, lambda: CronExpression.parse('* * * *'))
        self.assertRaises(ValueError, lambda: CronExpression.parse('* * * * * *'))

    def test_match_all(self):
        expr = CronExpression.parse('* * * * *')
        self._test_expression(expr, '2024-07-01 15:29:59', '2024-07-01 15:30:00')
        self._test_expression(expr, '2024-07-01 15:30:00', '2024-07-01 15:31:00')
        self._test_expression(expr, '2024-07-01 15:30:01', '2024-07-01 15:31:00')

    def test_match_specific(self):
        expr = CronExpression.parse('35 * * * *')
        self._test_expression(expr, '2024-07-01 15:34:00', '2024-07-01 15:35:00')
        self._test_expression(expr, '2024-07-01 15:35:01', '2024-07-01 16:35:00')
        self._test_expression(expr, '2024-07-01 15:40:00', '2024-07-01 16:35:00')

        expr = CronExpression.parse('35 15 * * *')
        self._test_expression(expr, '2024-07-01 14:31:00', '2024-07-01 15:35:00')
        self._test_expression(expr, '2024-07-01 15:30:00', '2024-07-01 15:35:00')
        self._test_expression(expr, '2024-07-01 15:35:00', '2024-07-02 15:35:00')
        self._test_expression(expr, '2024-07-01 23:35:01', '2024-07-02 15:35:00')

        expr = CronExpression.parse('35 15 1 * *')
        self._test_expression(expr, '2024-07-01 14:30:00', '2024-07-01 15:35:00')
        self._test_expression(expr, '2024-07-01 15:30:00', '2024-07-01 15:35:00')
        self._test_expression(expr, '2024-07-01 15:35:00', '2024-08-01 15:35:00')
        self._test_expression(expr, '2024-07-01 23:35:01', '2024-08-01 15:35:00')

        expr = CronExpression.parse('35 15 5 7 *')
        self._test_expression(expr, '2024-06-30 15:34:00', '2024-07-05 15:35:00')
        self._test_expression(expr, '2024-07-01 18:34:00', '2024-07-05 15:35:00')
        self._test_expression(expr, '2024-07-05 15:35:00', '2025-07-05 15:35:00')
        self._test_expression(expr, '2024-08-01 15:34:00', '2025-07-05 15:35:00')

        expr = CronExpression.parse('35 15 5 7 4')
        self._test_expression(expr, '2024-06-30 15:34:00', '2024-07-05 15:35:00')
        self._test_expression(expr, '2024-06-30 16:36:00', '2024-07-05 15:35:00')
        self._test_expression(expr, '2024-07-01 14:34:00', '2024-07-05 15:35:00')
        self._test_expression(expr, '2024-07-05 14:34:00', '2024-07-05 15:35:00')
        self._test_expression(expr, '2024-07-05 15:35:00', '2030-07-05 15:35:00')
        self._test_expression(expr, '2024-08-05 15:35:00', '2030-07-05 15:35:00')

    def test_leap_year(self):
        expr = CronExpression.parse('35 15 29 2 *')
        self._test_expression(expr, '2024-01-30 15:34:00', '2024-02-29 15:35:00')
        self._test_expression(expr, '2024-02-01 15:34:00', '2024-02-29 15:35:00')
        self._test_expression(expr, '2023-02-01 15:34:00', '2024-02-29 15:35:00')
        self._test_expression(expr, '2020-03-01 15:34:00', '2024-02-29 15:35:00')
        self._test_expression(expr, '1900-01-01 15:34:00', '1904-02-29 15:35:00')
        self._test_expression(expr, '2000-01-01 15:34:00', '2000-02-29 15:35:00')

        expr = CronExpression.parse('35 15 29 2 4')
        self._test_expression(expr, '2024-01-01 15:34:00', '2036-02-29 15:35:00')

    def test_step(self):
        expr = CronExpression.parse('*/4 5 * * *')
        last = '2024-01-01 15:34:00'
        last = self._test_expression(expr, last, '2024-01-02 05:00:00')
        last = self._test_expression(expr, last, '2024-01-02 05:04:00')
        last = '2024-01-01 05:10:00'
        last = self._test_expression(expr, last, '2024-01-01 05:12:00')
        last = self._test_expression(expr, last, '2024-01-01 05:16:00')
        last = self._test_expression(expr, last, '2024-01-01 05:20:00')

        expr = CronExpression.parse('30 */3 * * *')
        last = '2024-01-01 14:34:00'
        last = self._test_expression(expr, last, '2024-01-01 15:30:00')
        last = self._test_expression(expr, last, '2024-01-01 18:30:00')

        expr = CronExpression.parse('30 5 */2 * *')
        last = '2024-01-01 01:34:00'
        last = self._test_expression(expr, last, '2024-01-01 05:30:00')
        last = self._test_expression(expr, last, '2024-01-03 05:30:00')

        expr = CronExpression.parse('30 5 2 */3 ?')
        last = '2024-01-01 01:34:00'
        last = self._test_expression(expr, last, '2024-01-02 05:30:00')
        last = self._test_expression(expr, last, '2024-04-02 05:30:00')

        expr = CronExpression.parse('30 5 ? 3 */3')
        last = '2024-03-04 01:34:00'
        last = self._test_expression(expr, last, '2024-03-04 05:30:00')
        last = self._test_expression(expr, last, '2024-03-07 05:30:00')
        last = self._test_expression(expr, last, '2024-03-10 05:30:00')
        last = self._test_expression(expr, last, '2024-03-11 05:30:00')

    def test_range(self):
        expr = CronExpression.parse('15-17 5 * * *')
        last = '2024-01-01 00:00:00'
        last = self._test_expression(expr, last, '2024-01-01 05:15:00')
        last = self._test_expression(expr, last, '2024-01-01 05:16:00')
        last = self._test_expression(expr, last, '2024-01-01 05:17:00')
        last = self._test_expression(expr, last, '2024-01-02 05:15:00')

        expr = CronExpression.parse('30 11-13 * * *')
        last = '2024-01-01 00:00:00'
        last = self._test_expression(expr, last, '2024-01-01 11:30:00')
        last = self._test_expression(expr, last, '2024-01-01 12:30:00')
        last = self._test_expression(expr, last, '2024-01-01 13:30:00')
        last = self._test_expression(expr, last, '2024-01-02 11:30:00')
        
        expr = CronExpression.parse('30 11 3-5 * *')
        last = '2024-01-01 00:00:00'
        last = self._test_expression(expr, last, '2024-01-03 11:30:00')
        last = self._test_expression(expr, last, '2024-01-04 11:30:00')
        last = self._test_expression(expr, last, '2024-01-05 11:30:00')
        last = self._test_expression(expr, last, '2024-02-03 11:30:00')

        expr = CronExpression.parse('30 11 3-4 4-5 *')
        last = '2024-01-01 00:00:00'
        last = self._test_expression(expr, last, '2024-04-03 11:30:00')
        last = self._test_expression(expr, last, '2024-04-04 11:30:00')
        last = self._test_expression(expr, last, '2024-05-03 11:30:00')
        last = self._test_expression(expr, last, '2024-05-04 11:30:00')
        last = self._test_expression(expr, last, '2025-04-03 11:30:00')

        expr = CronExpression.parse('30 11 1-5 4-5 2-3')
        last = '2024-01-01 00:00:00'
        last = self._test_expression(expr, last, '2024-04-03 11:30:00')
        last = self._test_expression(expr, last, '2024-04-04 11:30:00')
        last = self._test_expression(expr, last, '2024-05-01 11:30:00')
        last = self._test_expression(expr, last, '2024-05-02 11:30:00')

    def test_combined(self):
        expr = CronExpression.parse('30 */3 1-10/5,13 4,7,9 2-3')
        self._test_expression(expr, '2024-07-10 22:55:23', '2026-04-01 00:30:00')
        

if __name__ == '__main__':
    unittest.main()