# DAO

The package provides an internal view into how data looks to the KeyService
itself; server-side only.

The DAO interface defines what any datastore must implement. These methods 
have no business logic. Store methods can create and/or update. The rest are
listed as obvious. (load is a get method, and the other are searches.)

The model is intended to be used by the managers. Calls to the managers and from the 
managers to its dependencies (Accessors and DAOs) should use the model.

# Changable

The @Changable annotation indicates what fields in the model can be
modified after creation. We store these values here, but they are followed
by the managers themselves. The DAO only modifies the creation date and update date.

# Model

The usage pattern for the model is basically this:
1. Owners have many keys.
2. Keys have versions which can be active. Consider key rotation; once the new key is in
use, the old key is deactivated.
3. There are key groups to be defined. This way all keys for a specific application or purpose
can be grouped together.

The identifier structure in the model is used to enforce a strict data type on these objects.
It's needed since most of these concepts are just strings, except for version.
We actually do use inheritance for the identifier model to assist with the lookup calls.
