from datetime import datetime
import sqlalchemy
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, ForeignKey
from sqlalchemy.types import DateTime, Integer, String
from sqlalchemy.orm import sessionmaker, relationship, backref
from faker import Faker

eng = create_engine("sqlite:///:memory:")

Base = declarative_base()

Session = sessionmaker(bind=eng)

faker = Faker()

class Comment(Base):
    __tablename__ = "comments"
    id = Column(Integer, primary_key=True)
    text = Column(String(140))
    author = Column(String(32))
    timestamp = Column(DateTime(), default=datetime.utcnow)
    left = Column(Integer)
    right = Column(Integer)
    level = Column(Integer)

def insert_fake_data(session):
    c1 = Comment(text=faker.text(100), author=faker.name(), left=0, right=3, level=0)
    c2 = Comment(text=faker.text(100), author=faker.name(), left=1, right=1, level=1)
    c3 = Comment(text=faker.text(100), author=faker.name(), left=2, right=3, level=1)
    c4 = Comment(text=faker.text(100), author=faker.name(), left=3, right=3, level=2)
    c5 = Comment(text=faker.text(100), author=faker.name(), left=4, right=5, level=0)
    c6 = Comment(text=faker.text(100), author=faker.name(), left=5, right=5, level=1)
    session.add_all([c1, c2, c3, c4, c5, c6])
    session.commit()

Base.metadata.bind = eng
Base.metadata.create_all()

def display_comments(session):
    for comment in session.query(Comment).order_by(Comment.left).all():
        print("%s▹%s %s by %s" % 
            ("  " * comment.level, comment.text, comment.timestamp.date(), comment.author))

if __name__ == "__main__":
    session = Session()
    insert_fake_data(session)
    display_comments(session)
