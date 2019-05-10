# queryenrichment

Experiments for a small framework to query results from the database and then enriching the result with
additional information from other data sources (e.g. internal caches).

## Background

I've recently found some code which repeatedly:

1. Queries entities from a database (can be a couple of hundred)
2. Runs the results through an ORM to get entity objects
3. Selects a couple of specific fields from the entity objects and puts it into an object array (Object[])
4. Adds additional information from other data sources (e.g. internal caches for data which changes rapidely
    and isn't persisted in the DB) into the object array
5. Returns an array of object arrays [Object[][]) as result of the operation

The assumption is that two improvements are possible:

1. Remove the ORM as there is no point in querying all data from the database and mapping all fields if only a few
fields are selected in step 3
2. If the filter criteria already conveys information then there is no need to query the field. E.g. if a search is run with
"WHERE firstName = 'John'" then you can safely assumes for all returned rows that the first name is "John". This might
have no big impact on with a database, but if the field is a more expensive result from one on the other data sources then
it easily gets important to avoid double effort here.

This project is an experimation area to toy around with those ideas.

## Preliminary results

So far the performance results seem to indicate that this approach might work better. The coding effort compared to an
ORM is of course much higher, but at the same time the ORM is only one of the steps described above. Therefore the coding
effort might be comparable to other solutions.

## Build

```
mvn clean install
```