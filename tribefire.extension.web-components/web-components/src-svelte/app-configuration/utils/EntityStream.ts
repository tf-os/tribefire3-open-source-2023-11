import { NEVER, Observable } from 'rxjs';
import { SvelteStartStopSubject } from './SvelteSubject';

type GenericEntity = $T.com.braintribe.model.generic.GenericEntity;

let warnedAboutEntityAsObservablePerformance = false;

const NULL_SUBJECT = new SvelteStartStopSubject(null);

export const entityAsObservable =
  function
    <T extends GenericEntity>
    (
      entity: T | null
    )
    : Observable<T> | null {

    if (!warnedAboutEntityAsObservablePerformance) {
      warnedAboutEntityAsObservablePerformance = true;
      console.warn(`Subscribing to an entity will rerender all components on any entity's property change.\nFor better performance use propertyAsWritableStream(entity, propertyName) or propertyAsReadableStream(entity, propertyName)`);
    }

    if (!entity) return NULL_SUBJECT;

    if ((entity as any)._selfObservable) return (entity as any)._selfObservable;

    let listener;
    let subject;

    subject = new SvelteStartStopSubject(entity, {
      onStart: () => {
        // listener = new $tf.manipulation.ManipulationListener();
        // listener.noticeManipulation = () => {
        //   subject.next(entity);
        // };
        // entity.Session().listeners().entity(entity).add(listener);

        entity.Session().listeners().entity(entity).add((m: any) => {
          console.log({ m })
          subject.next(entity);
        });
      },
      onStop: () => {
        entity?.Session()?.listeners()?.entity(entity).remove(listener);
      },
      // onNext: nextValue => {
      //   entity[propertyName] = nextValue;
      // }
    });

    (entity as any)._selfObservable = subject.asObservable();

    return subject.asObservable();
  };

export const set =
  function
    <T extends GenericEntity>
    (
      entity: T | null,
      nextValue
    )
    : void {

    // NOOP
  };

export const propertyAsWritableStream =
  function
    <
      T extends GenericEntity,
      Key extends keyof T
    >
    (
      entity: T | null,
      propertyName: Key
    )
    : SvelteStartStopSubject<T[Key]> | null {

    if (!entity) return NULL_SUBJECT;

    if (!(entity as any)._propertyNameToSubjectMap) {
      (entity as any)._propertyNameToSubjectMap = new Map();
    }

    const propertyNameToSubjectMap = (entity as any)._propertyNameToSubjectMap;

    if (propertyNameToSubjectMap.has(propertyName)) return propertyNameToSubjectMap.get(propertyName);

    let listener;
    let subject;
    let ignoreNextStreamEmit = false
    let ignoreNextListener = false

    // console.log(`CREATE propertyAsWritableStream: "${propertyName}"`)
    let isTfEntity = false;
    
    subject = new SvelteStartStopSubject(entity[propertyName], {
      onStart: () => {
        const session = entity?.Session();
        if (session) {
          isTfEntity = true;
          session.listeners().entityProperty(entity, propertyName).add(manipulation => {
            if (ignoreNextListener) {
              ignoreNextListener = false;
              return;
            }

            ignoreNextStreamEmit = true
            subject.next(entity[propertyName]);
          });
        }
      },
      onStop: () => {
        if (isTfEntity) entity?.Session()?.listeners()?.entityProperty(entity, propertyName).remove(listener);
      },
      onNext: nextValue => {
        if (ignoreNextStreamEmit) {
          ignoreNextStreamEmit = false;
          return;
        }

        if (entity[propertyName] !== nextValue) {
          ignoreNextListener = true;
          entity[propertyName] = nextValue;
        }
      }
    });

    propertyNameToSubjectMap.set(propertyName, subject);

    return subject;
  };

export const propertyAsReadableStream =
  function
    <
      T extends GenericEntity,
      Key extends keyof T
    >
    (
      entity: T | null,
      propertyName: Key
    )
    : Observable<T[Key]> | null {

    if (entity) {
      return (propertyAsWritableStream(entity, propertyName) as SvelteStartStopSubject<T[Key]>).asObservable();
    }
    return NEVER
  };
