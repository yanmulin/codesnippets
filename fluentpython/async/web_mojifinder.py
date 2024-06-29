import unicodedata
from fastapi import FastAPI
from fastapi.responses import HTMLResponse
from charindex import InvertedIndex
from pathlib import Path
from pydantic import BaseModel

app = FastAPI(
    title='Mojifinder Web'
)

class CharName(BaseModel):  # <2>
    char: str
    name: str

def init(app: FastAPI):
    app.state.index = InvertedIndex()
    static = Path(__file__).parent.absolute() / 'static'
    app.state.form = (static / 'form.html').read_text()
    
init(app)

@app.get('/search', response_model=list[CharName])
async def search(q: str):
    chars = app.state.index.search(q)
    return ({'char': c, 'name': unicodedata.name(c)} for c in chars)

@app.get('/', response_class=HTMLResponse, include_in_schema=False)
def form():  # <9>
    return app.state.form