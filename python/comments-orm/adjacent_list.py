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
    parent_id = Column(Integer, ForeignKey("comments.id"))
    replies = relationship("Comment", backref=backref("parent", remote_side=[id]))

Base.metadata.bind = eng
Base.metadata.create_all()

def insert_fake_data(session, num_rows):
    c1 = Comment(text=faker.text(20), author=faker.name(), parent=None)
    c2 = Comment(text=faker.text(20), author=faker.name(), parent=c1)
    c3 = Comment(text=faker.text(20), author=faker.name(), parent=c1)
    c4 = Comment(text=faker.text(20), author=faker.name(), parent=c3)
    c5 = Comment(text=faker.text(20), author=faker.name(), parent=c1)
    c6 = Comment(text=faker.text(20), author=faker.name(), parent=c5)
    c7 = Comment(text=faker.text(20), author=faker.name(), parent=None)
    session.add_all([c1, c2, c3, c4, c5, c6, c7])
    session.commit()

def display_comment(session, comment, level):
    print("%s▹%s %s by %s" % 
        ("  " * level, comment.text, comment.timestamp.date(), comment.author))
    for comment in session.query(Comment).filter_by(parent_id=comment.id).all():
        display_comment(session, comment, level + 1)

def display_comments():
    session = Session()
    for comment in session.query(Comment).filter_by(parent=None).all():
        display_comment(session, comment, 0)

if __name__ == "__main__":
    insert_fake_data(Session(), 100)
    display_comments()