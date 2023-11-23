export function lazyLoadProperty(entity: $T.com.braintribe.model.meta.GmEntityType, propertyName: string) {
  return lazyLoadTC(entity, propertyName, $tf.tc.create().negation().joker().done())
    .then(data => {
      if (entity) {
        const session = entity.Session();
        if (session.suspendHistory) session.suspendHistory();
        entity[propertyName] = data;
        if (session.resumeHistory) session.resumeHistory();
      }
    })
}

export async function lazyLoadTC(entity: $T.com.braintribe.model.meta.GmEntityType, propertyName: string, tc: $T.com.braintribe.model.query.Query.traversingCriterion) {
  if (!entity) return null;

  const property: $tf.reflection.Property = entity.Type().findProperty(propertyName);

  if (property && property.isAbsent(entity)) {
    try {
      let q
      if (!isNaN(Number(entity.id.toString()))) {
        q = $tf.query.parse("property " + propertyName + " of reference(" + entity.Type().getTypeSignature() + ", " + entity.id + ")");
      } else {
        q = $tf.query.parse("property " + propertyName + " of reference(" + entity.Type().getTypeSignature() + ", '" + entity.id + "')");
      }
      // const q = $tf.query.parse("property " + propertyName + " of reference(" + entity.Type().getTypeSignature() + ", '" + entity.id + "')");
      q.traversingCriterion = tc
      const res = await (entity.Session() as $tf.session.PersistenceGmSession).query().property(q)
      // if (res) entity[propertyName] = res.value()
      return res.value()
    } catch (error) {
      console.error({ error })
    }


  } else {
    return property ? entity[propertyName] : null
  }
}

export async function deleteEntity(entity: any) {
  entity?.Session()?.deleteEntity(entity);
}
