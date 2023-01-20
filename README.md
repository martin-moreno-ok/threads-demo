# Getting Started

### General Notes
I implemented the pool using a BlockingQueue type because it's thread safe. 
As requested I implemented the code using Generics so all resources implementation 
that extends from the Resource class can be used on this pool

I created some tests to cover all possible paths I could figure

Finally, I made some synchronized methods to protect the code when
I do stress tests.
