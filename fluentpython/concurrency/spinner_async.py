import asyncio
import itertools
import time

async def spin(msg):
    for char in itertools.cycle(r'\|/-'):
        status = f'\r{char} {msg}'
        print(status, flush=True, end='')
        try:
            await asyncio.sleep(.1)
        except asyncio.CancelledError:
            break
    blank = ' ' * len(status)
    print(f'\r{blank}\r', end='')

async def slow():
    time.sleep(3)
    return 42

async def supervisor():
    spinner = asyncio.create_task(spin('thinking'))
    print(f'spinner object: {spinner}')
    result = await slow()
    spinner.cancel()
    return result

def main():
    result = asyncio.run(supervisor())
    print(f'Answer: {result}')

if __name__ == '__main__':
    main()