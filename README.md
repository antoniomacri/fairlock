FairLock
========

Implementazione della classe `FairLock` e di variabili condizione con semantica _signal-and-urgent-wait_.

Academic project, _Sistemi Operativi e Programmazione Distribuita_, Laurea Magistrale in Ingegneria Informatica, Università di Pisa.



Specifiche
----------

La prima parte del progetto consiste nella implementazione del meccanismo di sincronizzazione tipico dei monitor (mutua esclusione e variabili condition) che, con riferimento alla primitiva `signal` utilizzi la semantica _signal-and-urgent_.

Per l'implementazione del meccanismo devono essere utilizzati esclusivamente i costrutti di sincronizzazione offerti da Java 1.4.2 (e cioè i metodi e/o i blocchi `synchronized` e i metodi `wait()`, `notify()` e `notifyAll()` offerti dalla classe `Object`).

In altri termini, si chiede di implementare un meccanismo simile a quello direttamente offerto dalle versioni più recenti di Java ma con una diversa semantica. In particolare, il meccanismo direttamente offerto dalle versioni più recenti di Java consente di utilizzare oggetti di tipo `Lock` per programmare esplicitamente la mutua esclusione fra le esecuzioni dei metodi di accesso ad un oggetto condiviso fra più threads. Inoltre, oggetti di tipo `Lock` consentono anche di dichiarare variabili di tipo `Condition` su cui eseguire le operazioni primitive `c.await()`, `c.signal()` e `c.signalAll()` usate rispettivamente per bloccare un thread sulla variabile `Condition c`, per svegliare un thread bloccato sulla variabile `Condition c`, o per svegliare tutti i thread sospesi su tale variabile. In questo caso però la semantica offerta da Java è la _signal-and-continue_. Nel progetto, si chiede di fornire un analogo meccanismo cambiando però la semantica offerta, secondo, le specifiche indicate di seguito.

  * **REQUISITO N.1** Implementare un meccanismo di mutua esclusione da utilizzare per programmare esplicitamente la mutua esclusione fra le esecuzioni dei metodi di accesso ad un oggetto condiviso fra più threads. Cioè, a differenza della mutua esclusione implicita offerta dai blocchi `synchronized`, la garanzia di mutua esclusione è demandata al programmatore e cioè programmata esplicitamente. In particolare: implementare la classe `FairLock` che offra i due metodi, `lock()` ed `unlock()` da usare per programmare esplicitamente gli accessi esclusivi ad un oggetto condiviso.

  * **REQUISITO N.2]** A differenza del _wait set_ di un oggetto (contenente i thread sospesi per mutua esclusione nell'accesso ad un blocco `synchronized`) che, come noto, non prevede nessun tipo di gestione, i thread che si sospendono nel tentativo di accedere ad un oggetto condiviso eseguendo il metodo `mutex.lock()` (dove `mutex` rappresenta un'istanza della classe `FairLock` utilizzata per garantire la mutua esclusione negli accessi all'oggetto condiviso) devono essere risvegliati in ordine FIFO.

  * **REQUISITO N.3** Implementare la classe `Condition` che offre i metodi `await()` e `signal()` usati, rispettivamente, per sospendere un thread su una variabile `Condition` e per risvegliare il primo che si è sospeso (se c'è) fra i thread presenti sulla variabile `Condition`. Al solito, se nessun thread è sospeso sulla variabile `Condition`, il metodo `signal()` non esegue nessuna operazione. Quindi la `Condition` deve fornire una coda FIFO di thread sospesi. La semantica della `signal` deve essere la _signal-and-urgent_. Per questo motivo, non è prevista l'implementazione del metodo `signalAll()`.

  * **REQUISITO N.4** Come noto dalla teoria dei monitor, una variabile `Condition` è sempre locale ad un monitor e le operazioni primitive `await()` e `signal()` possono essere quindi utilizzate solo all'interno dei metodi di accesso ad un monitor. Ciò implica che ogni istanza della classe `Condition` deve essere intrinsecamente legata ad un `lock` (oggetto della classe `FairLock`). Per questo motivo la classe `FairLock` deve offrire, oltre ai metodi `lock()` ed `unlock()` prima visti, anche il metodo `newCondition()`. In questo modo, se ad esempio `mutex` è un istanza della classe `FairLock`, l'operazione `mutex.newCondition()` restituisce un'istanza di `Condition` legata allo specifico oggetto `mutex` di tipo `Lock`.


Presentazione del lavoro
------------------------

Nel contesto in cui si inquadra questo progetto, la _fairness_ è intesa come una soluzione al problema della _starvation_, ovvero alla condizione in cui un processo può venire ritardato per un tempo indefinito. In Java questo può accadere per due motivi (oltre a questioni legate alla priorità dei processi):

  1. quando un thread rimane in attesa di entrare in un blocco/metodo sincronizzato, a causa di altri processi che lo precedono;
  2. quando un thread si è sospeso su una `wait()` e vengono risvegliati sempre altri thread.

I blocchi sincronizzati in Java infatti non danno alcuna garanzia sull'ordine seguito dai thread in attesa nell'_entry-set_, quindi è teoricamente possibile che un thread rimanga bloccato indefinitamente. Neanche il metodo `notify()` offre garanzie su quale thread venga risvegliato, perciò è teoricamente possibile che un thread rimanga sempre in attesa perché viene sempre preceduto da altri thread.

Se i metodi sincronizzati che acquisiscono il lock sono brevi, come per esempio il metodo `lock()` della classe `FairLock` presentata in questo documento, è verosimile che un thread che attende di acquisire il lock ed entrare nella sezione critica rimanga bloccato per la maggior parte del tempo non in attesa di eseguire il metodo `lock()` in sé, ma sulla `wait()` in esso contenuta.

La classe `FairLock`, come direttamente o indirettamente tutti i meccanismi di sincronizzazione programmati esplicitamente, si basano necessariamente su blocchi sincronizzati, sui quali non è possibile intervenire per migliorare la _fairness_. Nonostante ciò, si può comunque lavorare sulla `wait()` e far sì che l'attesa nell'accesso a un lock esplicito e l'attesa su una variabile condizione seguano una politica FIFO, garantendo così l'assenza di starvation.

Come sarà illustrato più avanti, ciò avviene facendo sì che ciascun thread invochi una `wait()` su un oggetto separato: in questo modo un solo thread è in attesa su un oggetto e di conseguenza è possibile attivare selettivamente uno specifico thread.

Per implementare la classe `FairLock` si è sviluppato un _package_ Java costituito di tre classi suddivise in altrettanti file:

  * `FairLock.java`: contiene la realizzazione della classe principale;
  * `Condition.java`: contiene la realizzazione delle variabili condizione che vanno usate insieme alla classe `FairLock`;
  * `QueueNode.java`: una classe di utilità richiesta per implementare le due classi precedenti.

Le prime due classi sono pubbliche, accessibili esternamente al _package_, mentre la classe `QueueNode` è _package-private_. Inoltre, il costruttore della classe `Condition` è anch'esso _package-private_ in quando istanze di tale classe devono essere create sempre e solamente tramite un'istanza della classe `FairLock`. Come illustrato più avanti, anche altri metodi hanno visibilità ridotta, in quanto si è scelto come regola generale di mantenere per ciascun identificatore la minima visibilità necessaria.


La classe `FairLock`
--------------------

```java
public class FairLock {
    Thread owner = null;
    private final Object mutex = new Object();
    private ArrayDeque entryQueue = new ArrayDeque();
    ArrayDeque urgentQueue = new ArrayDeque();

    boolean isLocked() {
        return owner != null;
    }

    public void lock() throws InterruptedException {
        ...
        synchronized(mutex) { ... }
        ...
    }

    public void unlock() {
        ...
        synchronized(mutex) { ... }
        ...
    }

    public Condition newCondition() {
        return new Condition(this);
    }
}
```

**Listato 1.** Struttura generale della classe `FairLock`.


**Struttura e criteri generali.**
Nel listato **1** viene mostrata la struttura generale della classe `FairLock`, con la dichiarazione delle variabili membro. L'implementazione dei metodi `lock()` e `unlock()` è invece riportata più avanti nei listati **2** e **3**. Nei capoversi seguenti, prima di illustrarne in dettaglio il funzionamento, si enunciano alcuni criteri generali seguiti durante le fasi di progetto e implementazione della classe `FairLock` (e, per riflesso, anche delle altre due).

Per motivi di debugging, la classe `FairLock` tiene traccia non solo dello stato del lock (libero o occupato), ma anche del thread che in ciascun istante lo occupa. Viene perciò dichiarata una variabile `Thread owner` che contiene di volta in volta il thread che possiede il lock (o `null` se il lock è libero). Per maggiore espressività, viene anche definito il metodo `isLocked()`. Se il thread che ha chiamato la `lock()` cerca di invocarla nuovamente, viene lanciata un'eccezione di tipo `IllegalMonitorStateException` (il lock implementato, infatti, non è _rientrante_); in maniera complementare, viene lanciata un'eccezione dello stesso tipo se un thread che non detiene il lock cerca di rilasciarlo invocando il metodo `unlock()`.

I metodi fondamentali implementati dalla classe sono `lock()` e `unlock()`. Il primo può sollevare eccezioni di tipo `InterruptedException`: si è preferito non catturare tale eccezione all'interno del metodo, ma propagarla, in modo da dare all'utente la possibilità di gestirla secondo le proprie necessità.

I due metodi non sono dichiarati `synchronized`: si è preferito usare dei _blocchi_ sincronizzati anche laddove non erano necessari (nel metodo `unlock()` per esempio), e in particolare specificare la sincronizzazione non su `this`, bensì su un oggetto privato assegnato alla variabile membro `mutex`. In questo modo, se da un lato si perde (da parte dell'utente della classe) l'evidenza dei vincoli di sincronizzazione, si mette tuttavia la classe in sicurezza rispetto a usi erronei della stessa (cioè rispetto a codice utente che potrebbe usare da sé il monitor associato a `this`).

```java
public void lock() throws InterruptedException {
    Thread thisThread = Thread.currentThread();
    if (owner == thisThread) {
        throw new IllegalMonitorStateException(
            "The current thread has already locked this lock");
    }
    QueueNode node;
    synchronized(mutex) {
        if (!isLocked()) {
            owner = thisThread;
            return;
        }
        node = new QueueNode(thisThread);
        entryQueue.add(node);
    }
    try {
        node.doWait();
    } finally {
        synchronized(mutex) {
            entryQueue.remove(node);
        }
    }
}
```

**Listato 2.** La classe `FairLock`: implementazione del metodo `lock()`.

**Il metodo `lock()`.**
Come anticipato, all'interno del metodo `lock()`, si esegue per prima cosa un controllo di correttezza a fini di debugging: se il lock è già detenuto dal thread corrente, viene lanciata un'eccezione di `IllegalMonitorStateException` (_unchecked_). Infatti la classe `FairLock` implementa un lock non rientrante, ovvero che non permette di eseguire coppie di `lock()`/`unlock()` annidate.

Viene dichiarata una variabile locale di tipo `QueueNode`, sulla quale il thread corrente potrà bloccarsi (qualora il lock sia occupato). La classe `QueueNode` rappresenta sostanzialmente un semaforo evento, ovvero un semaforo binario inizializzato a zero, con una semplice aggiunta che sarà illustrata a breve.

Nelle righe seguenti, si entra dunque in un blocco sincronizzato per operare in mutua esclusione sulle strutture dati del monitor. Si controlla immediatamente lo stato del lock: se è libero, allora viene marcato come occupato dal thread corrente e si restituisce il controllo al chiamante, senza alcuna ulteriore operazione.

Se invece il monitor è già occupato, allora bisogna bloccare il thread corrente. Si crea un oggetto di tipo `QueueNode`, assegnandolo alla variabile `node` precedentemente dichiarata, e lo si accoda all'_entry-queue_ secondo la politica FIFO (si inserisce in coda e si preleva dalla testa).

È possibile a questo punto uscire dalla mutua esclusione e sospendere il thread corrente sul nodo/semaforo appena creato invocandone il metodo `doWait()`. La necessità di racchiudere tale chiamata entro un blocco `try-finally` è dovuta a due motivi: da un lato, siccome il thread corrente è appena stato svegliato, bisogna rimuovere il relativo nodo dall'`entryQueue`; inoltre, poiché `doWait()` può sollevare un'`InterruptedException`, che va propagata al chiamante, è necessario comunque trattarla localmente, rimuovendo ugualmente il nodo dalla coda (altrimenti porterebbe il monitor in uno stato inconsistente). Chiaramente, prima di intervenire sulla coda, essendo questa una risorsa astratta implementata non atomicamente (i suoi metodi non sono _thread-safe_) è necessario riacquisire il `mutex`.

All'uscita del blocco `try-finally` le strutture dati sono nuovamente consistenti e, poiché il thread corrente è stato risvegliato secondo lo schema con passaggio del testimone, come sarà mostrato più avanti, esso detiene già il lock.

In conclusione, per ciascun thread che invoca la `lock()` quando il monitor è occupato, viene creato un nuovo oggetto di tipo `QueueNode`. Il thread che chiamerà la `unlock` preleverà l'oggetto in cima alla coda e ne invocherà il metodo `doNotify()`, in modo da svegliare il thread in attesa. Viene dunque svegliato un solo thread, in maniera mirata, anziché tutti i thread come accadrebbe usando una `notifyAll()`, o uno qualsiasi come farebbe la `notify()`: in questo modo si realizza l'effettiva implementazione del criterio di _fairness_ sull'entry-queue del monitor.

Se al posto dell'oggetto di tipo `QueueNode` si dichiarasse un semplice `Object o`, usandone (dentro un blocco `synchronized(o)`) i metodi `o.wait()` e `o.notify()`, potrebbe sorgere un problema cosiddetto di _missed notify_ (_segnalazione persa_). Si supponga infatti che il thread che si sospende (il futuro thread segnalato) subisca preemption, all'interno della `lock()`, appena prima della `o.wait()`: se a questo punto andasse in esecuzione il thread segnalante e invocasse, nella `unlock()`, la `o.notify()`, quest'ultima troverebbe la _wait-queue_ vuota e non sortirebbe quindi alcun effetto; quando il thread segnalato riprenderebbe poi l'esecuzione si bloccherebbe erroneamente sulla `wait()`. È perciò necessario usare un semaforo evento, quale è implementato da `QueueNode`.

Un altro problema potrebbe sorgere se si eseguisse la `doWait()` all'interno del blocco sincronizzato su `mutex`. Non è possibile, infatti, eseguire chiamate innestate a procedure di monitor (un problema noto anche come di _nested lockout_). Si consideri il codice seguente:

```java
public void lock() {
    ...
    synchronized(mutex) {
        ...
        synchronized(o) {
            o.wait();
        }
    }
}

public void unlock() {
    ...
    synchronized(mutex) {
        ...
        synchronized(o) {
            o.notify();
        }
    }
}
```

Quando un thread si sospende sulla `o.wait()`, rilascia il blocco sul monitor associato all'oggetto `o` stesso, ma quello su `mutex` continua a essere occupato. Il thread che esegue la `unlock()` rimane sospeso su `mutex` e di conseguenza non riesce ad accedere al blocco sincronizzato su~`o`, necessario per invocare la `o.notify()` e sbloccare il primo thread.

Si può notare che un thread, prima di acquisire il lock oppure sospendersi perché il lock è già occupato, per garantire la consistenza degli stati interni del lock deve eseguire l'accesso al blocco `synchronized(mutex)`, ma per esso Java non garantisce alcun tipo di gestione (per esempio FIFO). Su questo meccanismo non è possibile intervenire: tuttavia, tipicamente il tempo in cui un thread rimane in attesa di accedere a quel blocco sincronizzato è nettamente inferiore al tempo di attesa per l'accesso all'oggetto condiviso che si vuole proteggere con il lock (ovvero, al tempo in cui il thread rimane bloccato sulla `node.doWait()`), per il quale viene invece garantito dalla classe `FairLock` un risveglio in ordine FIFO.

```java
public void unlock() {
    if (owner != Thread.currentThread()) {
        throw new IllegalMonitorStateException(
            "The current thread has not locked this lock");
    }
    synchronized(mutex) {
        QueueNode node;
        if (!urgentQueue.isEmpty()) {
            node = (QueueNode) urgentQueue.getLast();
        } else if (!entryQueue.isEmpty()) {
            node = (QueueNode) entryQueue.getFirst();
        } else {
            owner = null;
            return;
        }
        owner = node.thread;
        node.doNotify();
    }
}
```

**Listato 2.** La classe `FairLock`: implementazione del metodo `unlock()`.

**Il metodo `unlock()`.**
Anche qui, la prima istruzione eseguita è il controllo sul thread chiamante: viene lanciata un'eccezione di tipo `IllegalMonitorStateException` se il thread corrente non detiene il lock (perché il lock è libero, oppure perché è detenuto da un thread diverso).

Si accede quindi alla sezione critica. Se non ci sono thread sospesi né sulla _entry-queue_ né sulla _urgent-queue_, allora il monitor viene marcato come libero (`owner = null`), senza compiere ulteriori operazioni. Altrimenti bisogna risvegliare un thread tra quelli della _entry-queue_ o della _urgent-queue_, privilegiando questi ultimi. In particolare, se l'_urgent-queue_ non è vuota, se ne prende l'ultimo elemento (essa viene infatti gestita in modalità LIFO), altrimenti si sceglie il primo dall'_entry-queue_. Il thread selezionato viene subito marcato come il detentore del lock, dopodiché viene risvegliato: si segue lo schema con “passaggio del testimone”.

È chiara a questo punto l'utilità della classe `QueueNode`: essa serve anche a memorizzare l'identificatore del thread che ha creato l'oggetto, in modo da segnarlo come il proprietario del lock. Se la classe `FairLock` mantenesse una semplice variabile booleana a indicare se il monitor è occupato o no, un classico semaforo binario sarebbe sufficiente. Ma dal momento che si vuole tener traccia in ogni momento anche del thread che detiene il lock, creiamo appositamente una nuova classe.

Si noti infatti che non è possibile porre semplicemente `owner = null` nella `unlock()` e poi reimpostare l'`owner` all'interno della `lock()`. Infatti, se si operasse in questo modo, dopo essere usciti dal blocco sincronizzato nella `unlock()` si permetterebbe a un qualsiasi thread che si trovi all'interno della `lock()`, in attesa di entrare nel blocco sincronizzato, di trovarlo libero e acquisirlo, violando il criterio della sequenzialità FIFO per gli accessi al monitor.


La classe `Condition`
---------------------

Il listato **4** mostra la struttura generale della classe `Condition`, mentre l'implementazione dei metodi `await()` e `signal()` è riportata nei listati **5** e **6**. Avendo chiarito il funzionamento della `FairLock`, si possono ritrovare qui gli stessi schemi di base.

```java
public class Condition {
    private FairLock lock;
    private ArrayDeque queue = new ArrayDeque();

    public Condition(FairLock lock) {
        this.lock = lock;
    }

    public void await() throws InterruptedException {
        ...
    }

    public void signal() throws InterruptedException {
        ...
    }
}
```

**Listato 4.** Struttura generale della classe `Condition`.

**Il metodo `await()`.**
All'interno del metodo `await()`, dopo il solito controllo sulla correttezza dell'invocazione, si crea un nuovo oggetto `QueueNode` relativo al thread corrente, lo si aggiunge alla coda FIFO di thread sospesi sulla variabile condizione e si sospende effettivamente il thread, dopo aver rilasciato il lock. Il blocco `try-finally` serve, qui come nella `FairLock.lock()`, ad avere la garanzia di rimuovere il nodo dalla coda dei thread sospesi. Le operazioni sulla coda vanno svolte in mutua esclusione perché, in caso di interruzione del thread corrente, un'eccezione di `InterruptedException` potrebbe esser sollevata in qualsiasi istante, indipendentemente dallo stato del lock e da chi lo detiene.

```java
public void await() throws InterruptedException {
    Thread thisThread = Thread.currentThread();
    if (lock.owner != thisThread) {
        throw new IllegalMonitorStateException(
            "The current thread does not hold the lock "
                + "associated with this Condition");
    }
    QueueNode node = new QueueNode(thisThread);
    synchronized(queue) {
        queue.add(node);
    }
    lock.unlock();
    try {
        node.doWait();
    } finally {
        synchronized(queue) {
            queue.remove(node);
        }
    }
}
```

**Listato 5.** La classe `Condition`: implementazione del metodo `await()`.

**Il metodo `signal()`.**
L'implementazione della `signal()` segue la semantica _signal-and-urgent-wait_. Se non ci sono thread sospesi sulla variabile condizione (ovvero se il test `node != null` è falso), allora non viene eseguita alcuna operazione. Altrimenti, si riattiva il primo thread bloccato, eseguendo la `doNotify()` sul suo semaforo (equivalente a una `V()`), e si sospende il thread corrente in fondo alla _urgent-queue_. Si noti anche qui il “passaggio del testimone”: prima di risvegliare il thread bloccato, non si rilascia il lock, ma anzi si assegna il possesso del lock al thread segnalato.

```java
public void signal() throws InterruptedException {
    Thread thisThread = Thread.currentThread();
    if (lock.owner != thisThread) {
        throw new IllegalMonitorStateException(
            "The current thread does not hold the lock "
                + "associated with this Condition");
    }
    QueueNode node = (QueueNode) queue.peekFirst();
    if (node != null) {
        QueueNode urgentNode = new QueueNode(thisThread);
        lock.urgentQueue.addLast(urgentNode);
        lock.owner = node.thread;
        node.doNotify();
        try {
            urgentNode.doWait();
        } finally {
            lock.urgentQueue.removeLast();
        }
    }
}
```

**Listato 6.** La classe `Condition`: implementazione del metodo `signal()`.


La classe di utilità `QueueNode`
--------------------------------

La classe `QueueNode`, il cui codice è riportato per intero nel listato **7**, è stata realizzata con un triplice scopo:

  1. innanzitutto, come spiegato illustrando il funzionamento della `lock()`, serve a evitare un problema di _missed notify_ qualora venga eseguita la `notify()` di un generico `Object` prima della rispettiva `wait()`;
  2. inoltre, viene usata per conservare l'identificatore del thread associato al nodo, in modo da marcarlo immediatamente come il detentore del lock non appena il thread segnalante lo rilascerebbe;
  3. infine, serve come protezione contro _risvegli spuri_.

```java
package fairlock;

class QueueNode {
	private boolean notified = false;

	final Thread thread;

	public QueueNode(Thread thread) {
		this.thread = thread;
	}

	public synchronized void doWait() throws InterruptedException {
		while (!notified) {
			wait();
		}
		notified = false;
	}

	public synchronized void doNotify() {
		notified = true;
		notify();
	}
}
```

**Listato 7.** La classe `QueueNode`.

Per risolvere il primo problema, basta implementare un semaforo evento, in cui si mantiene una variabile booleana che indica se l'evento è stato segnalato e consumato. Il secondo problema viene invece risolto dichiarando una variabile membro `thread`: come si può notare essa non ha modificatori di accesso (è _package-private_ e non privata) in quanto deve essere accessibile dalle classi `FairLock` e `Condition`.

L'ultimo punto trova riscontro, nell'implementazione della `QueueNode`, con l'uso di un ciclo `while`: bisogna infatti tener conto dell'ineliminabile presenza di _risvegli spuri_ (_spurious wakeup_), a causa dei quali la `wait()` potrebbe terminare e restituire il controllo al chiamante senza alcun apparente motivo (e quindi senza che l'evento sia stato effettivamente segnalato).
