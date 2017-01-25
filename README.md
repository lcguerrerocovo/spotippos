## Spotippos Search API

This project was created to complete the requirements put forth in the following [coding challenge](https://github.com/VivaReal/code-challenge/blob/master/backend.md), my personal interest in completing the challenge was to gain experience developing real world Web applications using Scala.

It makes use of [Finch](https://github.com/finagle/finch) which is an API on top of Finagle for building HTTP services.

The initial property inventory is loaded from a [Json](https://github.com/lcguerrerocovo/spotippos/blob/master/properties.json) file provided in the coding challenge.

Properties are stored in a map in memory, a [Range Tree](https://en.wikipedia.org/wiki/Range_tree) is built for efficient search using coordinates that describe a rectangular viewport on a 2 dimensional integer map where the properties are located.

As soon as properties are added to the inventory they are searcheable by id, but in order for them to be discoverable via search a new Range Tree has to be constructed. There is a [recurring task](https://github.com/lcguerrerocovo/spotippos/blob/master/src/main/scala/com/vivareal/spotippos/Main.scala#L71-L77) that build this new Range Tree including all recently added properties if there exist new properties to be indexed.

The search efficiency is O(log(n)+k) where dimension is not taken into account since the current implementation only provides 2 dimensional search capabilities. It basically searches using binary search on the tree (the log(n) factor since the tree has (n*2)-1 nodes and n leaves) where each node has enough information so that we know if we need to continue searching left or right. Once the subset of inner trees is determined, we have to traverse these trees to return the leaf nodes (this is the k factor). 

With regards to space, each inner node has to store a complete binary tree representation which is a Range Tree in itself with coordinate values inverted, thus space complexity is of O(n^2).

The correctness of the properties of Range Tree behavior is tested [here](https://github.com/lcguerrerocovo/spotippos/blob/master/src/test/scala/com/vivareal/spotippos/RangeTreeSpec.scala).

To run the server using sbt just run

```
sbt run
```

Server will run on port [8081](https://github.com/lcguerrerocovo/spotippos/blob/master/src/main/scala/com/vivareal/spotippos/Main.scala#L22)

The following is a series of example queries with implemented functionality

####Search by id

```
curl -X GET "http://localhost:8081/properties/8000"

```

```json
{
  "id": 8000,
  "title": "Imóvel código 8000, com 5 quartos e 4 banheiros.",
  "price": 1316000,
  "description": "Incididunt sint commodo ad incididunt eu id elit reprehenderit pariatur voluptate Lorem anim esse eu. Eiusmod exercitation ex minim anim aliquip eu sunt exercitation deserunt culpa.",
  "lat": 358,
  "long": 536,
  "beds": 5,
  "baths": 4,
  "squareMeters": 128,
  "provinces": [
    "Gode"
  ]
}
```

####Search by viewport

```
curl -X GET "http://localhost:8081/properties?ax=500&ay=500&bx=515&by=515"
```

```json
{
  "foundProperties": 2,
  "properties": [
    {
      "id": 3348,
      "title": "Imóvel código 3348, com 5 quartos e 4 banheiros.",
      "price": 1569000,
      "description": "Amet exercitation est et voluptate deserunt excepteur. Lorem id laboris velit et ipsum deserunt.",
      "lat": 518,
      "long": 513,
      "beds": 5,
      "baths": 4,
      "squareMeters": 155,
      "provinces": [
        "Gode",
        "Ruja"
      ]
    },
    {
      "id": 1415,
      "title": "Imóvel código 1415, com 1 quartos e 1 banheiros.",
      "price": 420000,
      "description": "Excepteur commodo minim pariatur enim. Amet adipisicing anim pariatur esse fugiat voluptate consequat amet incididunt.",
      "lat": 505,
      "long": 505,
      "beds": 1,
      "baths": 1,
      "squareMeters": 38,
      "provinces": [
        "Gode",
        "Ruja"
      ]
    }
  ]
}
```

####Create a property

```
curl -X POST -H "Content-Type: application/json" -d '{
    "title": "Simply the best",
    "price": 1000000000,
    "description": "This is the best property ever",
    "lat": 666,
    "long": 666,
    "beds": 3,
    "baths": 2,
    "squareMeters": 61
}' "http://localhost:8081/properties"
```

```json
{
  "id": 8001,
  "title": "Simply the best",
  "price": 1000000000,
  "description": "This is the best property ever",
  "lat": 666,
  "long": 666,
  "beds": 3,
  "baths": 2,
  "squareMeters": 61,
  "provinces": [
    "Ruja"
  ]
}
```

####Search for recently created property (after it is indexed)

```
curl -X GET "http://localhost:8081/properties?ax=660&ay=660&bx=670&by=670"
```

```json
{
  "foundProperties": 1,
  "properties": [
    {
      "id": 8001,
      "title": "Simply the best",
      "price": 1000000000,
      "description": "This is the best property ever",
      "lat": 666,
      "long": 666,
      "beds": 3,
      "baths": 2,
      "squareMeters": 61,
      "provinces": [
        "Ruja"
      ]
    }
  ]
}
```





  

