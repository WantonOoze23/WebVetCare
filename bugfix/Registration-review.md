# Task 1 — Registration & Authentication: Review

Reviewed: branch `dev-42` @ `e19e9c1`

Overall this is a strong submission. The three-module split is correct and the
dependency direction is clean, MVI is applied consistently across every feature, and
several security decisions go beyond what the task asked for — BCrypt at cost 12,
`EncryptedSharedPreferences` for tokens, and real RS256 signing rather than a stub. The
tests that exist pass: `./gradlew :auth:testDebugUnitTest :user:testDebugUnitTest` is
green, 20 tests.

What follows is everything I found. **I have deliberately not written the solutions.**
Each item says what the problem is and why it matters; working out how to fix it is the
task. If you get stuck on one, ask me — but ask after you have formed an opinion, not
before.

---

## Before you start fixing anything

### 1. Add me as a collaborator

I cannot review your work properly right now. I tried to open a pull request on your
repository and GitHub refused: `must be a collaborator`. With read-only access I cannot
push a branch, open a PR, or leave review comments anchored to specific lines of your
code — which is how code review is supposed to work.

Please add me: **Settings → Collaborators → Add people → `Protodiv`**
(https://github.com/Protodiv), with Write access.

Until that is done, feedback has to arrive as documents like this one, which is a far
worse way to learn than comments sitting on the exact line that has the problem.

### 2. Your branch names carry no information

Your remote currently has `dev`, `dev-42`, and `master`.

`dev-42` does not say what is on it. In six months — or during grading, or when you come
back to fix one of the items below — nobody can tell what that branch holds without
reading ten commit messages. `dev` is worse: it reads like a permanent integration
branch while actually holding a single task, and it is now stale, pointing at an
ancestor of `dev-42`.

Use this from now on:

```
<type>/<issue-number>-<short-kebab-summary>
```

| Example | For |
|---|---|
| `feat/42-registration-auth` | this task |
| `fix/57-token-refresh-loop` | a bug fix |
| `refactor/61-dashboard-routes` | restructuring, no behaviour change |
| `chore/63-bump-agp` | build, tooling, dependencies |
| `docs/64-readme` | documentation only |

Types: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`.

One branch per task. Branch from `master`. Never commit directly to `master`. We keep
branches after merge, which means the name is the only lasting record of what a branch
held — one more reason it has to say something.

### 3. Every task must be delivered as a pull request

`master` is still at the initial commit. Nothing from this task has been merged, and no
pull request exists.

A task is not finished when the code is pushed. It is finished when there is a PR
against `master`. The PR is the unit of review — it is where the diff gets read, where
comments attach to specific lines, and where the reasoning behind a change is recorded
in one place instead of being scattered across commit messages.

Your PR description should say what you built, and call out any place you deliberately
deviated from the specification and why. Before you ask for review, read your own diff
top to bottom on GitHub. Leftover template files and scaffolding comments are much
easier to spot in a diff than in the editor.

---

## How to read this

| Severity | Meaning |
|---|---|
| **Blocking** | Must be fixed. Specification not met, or a defect that will bite in normal use. |
| **Medium** | Should be fixed. Real problems, but they do not block the task being accepted. |
| **Minor** | Worth doing. Cosmetic, hygiene, or documentation. |

IDs are stable, so we can refer to them: `APP-B1` = `:app`, Blocking, first.

| Module | Blocking | Medium | Minor |
|---|---|---|---|
| `:app` | 2 | 5 | 1 |
| `:auth` | 1 | 7 | 1 |
| `:user` | 0 | 4 | 0 |
| Project-wide | 0 | 1 | 3 |

---
---

# Module `:app`

## Blocking

### APP-B1 · `navigation/AppDestination.kt:7-12`, `feature/dashboard/UserDashboardScreen.kt:81` — the five core routes are not routes

`AppDestination` declares four destinations: `Landing`, `Login`, `Register`,
`Dashboard`. The specification defines five more — `/user`, `/become-doctor`,
`/doctor-profile`, `/become-patient`, `/patient-profile`.

Those five exist as a `DashboardRoute` sealed class swapped by a `when` inside
`UserDashboardScreen`. Visually it works. Structurally you have given up four things:

- **No back stack.** Pressing system back from Become Doctor does not return to the
  previous section — it exits the dashboard entirely.
- **No deep links.** `/doctor-profile` cannot be addressed. Nothing outside the
  dashboard can send the user there.
- **The animated transitions you configured do not apply.** You set up
  `transitionSpec` and `popTransitionSpec` in `NavigationController` correctly, and then
  five of your nine screens never benefit from them.
- **`UserDashboardScreen.kt` is 357 lines holding six screens.** That is a direct
  consequence: because they are not destinations, they have nowhere else to live.

### APP-B2 · `MainActivity.kt:24-35` — Koin is started inside an Activity

Two problems, one line apart.

**The guard on line 24.** `if (GlobalContext.getOrNull() == null)` is there because
without it the app crashes. I verified that on a device: remove the check and the very
first configuration change — rotation, dark mode, font size, system language — dies
immediately with:

```
FATAL EXCEPTION: main
java.lang.RuntimeException: Unable to start activity ComponentInfo{...MainActivity}:
    org.koin.core.error.KoinApplicationAlreadyStartedException: A Koin Application has already been started
    at android.app.ActivityThread.handleRelaunchActivity(ActivityThread.java:6581)
Caused by: org.koin.core.error.KoinApplicationAlreadyStartedException
    at org.koin.core.context.GlobalContext.register(GlobalContext.kt:44)
    at com.tyshko.webvetcare.MainActivity.onCreate(MainActivity.kt:24)
```

So you read the symptom correctly, and the guard does stop the crash. The problem is
what it does about it: it avoids the crash by skipping initialisation, which means
whatever was captured on the very first run is kept for good. A loud, immediate,
impossible-to-miss failure has become a silent one — from the second configuration
change onward the app runs normally while holding on to something it should have
released.

**Line 26, `androidContext(this@MainActivity)`.** Whatever you pass here is stored by
Koin as a singleton definition in its global registry, and that registry lives as long
as the process. This Activity instance is therefore held for the entire life of the app
and can never be collected.

The consequence that matters more than the memory: after a configuration change the
Activity is destroyed and a new one created, but Koin still holds the original. Every
resolution after that point — both Room database builders, and `TokenStorage` — receives
a **destroyed** Activity. Nothing throws, because a destroyed Activity is still a usable
`ContextWrapper`. You will never see this by clicking through the app.

Two questions to sit with: why does this code run more than once in a single process,
and is there somewhere in the Android lifecycle that genuinely runs once per process?
And: what is the lifetime of the objects in this graph, and does the `Context` you hand
them have a lifetime that matches?

## Medium

### APP-M1 · `navigation/NavigationController.kt:26-35` — session restore is application logic living inside a composable

First, to remove a red herring: `remember {}` with no keys **does** execute — once, on
first composition, and the result is cached. The logic runs. That is not the problem.

The problems are:

- **It cannot be tested.** Deciding whether a returning user is still authenticated —
  read the token, validate the signature, attempt a refresh — is application logic. Here,
  testing it means running a composition.
- **It blocks the main thread.** That chain performs `EncryptedSharedPreferences`
  initialisation, which is slow on first access, plus an RSA-2048 signature —
  synchronously, during first composition.
- **There is no loading state.** The app must decide Landing-versus-Dashboard before it
  can draw anything, so a slow first read shows the user a blank frame.

### APP-M2 · `navigation/NavigationController.kt:49-83` and all four screens — two parallel mechanisms for navigation

You built an effect channel, and it already carries navigation:

```kotlin
// RegistrationScreen.kt:41-45
viewModel.effectFlow.collect { effect ->
    when (effect) {
        is RegisterContract.Effect.NavigateToDashboard -> onNavigateToDashboard()
        is RegisterContract.Effect.NavigateToLogin     -> onNavigateToLogin()
```

The ViewModel emits an effect, the screen catches it, and calls a lambda that
`NavigationController` passed down purely to turn it back into a back-stack operation.
Two mechanisms doing one job.

Why it matters: it does not scale. Every new screen adds a parameter to a signature, a
lambda at the call site, and a new `Effect` subtype — and `APP-B1` is about to add five
screens. The `backStack.clear()` + `add()` pairing is already copy-pasted three times.

One more thing in the same area: your effect collection uses `LaunchedEffect(Unit)`,
which keeps collecting while the app is in the background. A `Channel` delivers each
item to exactly one consumer, so an effect emitted while backgrounded is taken off the
channel and never seen by the user.

### APP-M3 · `feature/landing/LandingScreen.kt:23` — a repository injected straight into a composable

```kotlin
settingsRepository: SettingsRepository = koinInject()
```

The screen then holds `welcomeMessage` and `particleColorHex` as local state and
registers a config-update listener from a `LaunchedEffect`.

Every other screen in this app has a ViewModel, which is why this stands out — you know
the pattern, it just was not applied here. The costs: this screen cannot be previewed or
tested without a live Koin graph and a real Firebase instance, and the config-reading
logic is now duplicated with `SettingsViewModel` rather than shared.

### APP-M4 · `feature/dashboard/DashboardContract.kt:9-16,36` — navigation concepts living in one feature's contract

`DashboardRoute` is a navigation concept scoped inside a single feature's contract.
Nothing outside the dashboard can route to `/doctor-profile`, and it duplicates the job
`AppDestination` already exists to do. Every screen in the app needs to be reachable, not
only from the dashboard.

Note also `val title: String` on the route — that is UI copy embedded in a route
definition. It cannot be localised, and the route now knows about presentation.

Separately, `object NavigateToLogin : Effect()` — one effect per destination. Today that
is one; with the five screens from `APP-B1` it becomes six, each needing a branch in
every `when` that handles effects. Ask yourself what this sealed class looks like at
twenty screens.

### APP-M5 · `feature/auth/register/RegisterViewModel.kt:41` — `currentState` is not a snapshot

```kotlin
val currentState = _registerState
```

This assigns the `MutableStateFlow` itself, not its value. Every `currentState.value`
re-reads live state. The name says "current state"; the behaviour is "the state holder".
As written the variable earns nothing — it is an alias.

It is not only cosmetic. Look at the ordering:

```kotlin
val result = registerUseCase(email = currentState.value.email, ...)   // suspends
...
val newUser = User(userName = currentState.value.userName, ...)       // read AFTER
```

`userName` is read *after* the suspend point. If the user edits that field while
registration is in flight, the account is saved with the email that was submitted and
the username typed afterwards.

## Minor

### APP-N1 · `feature/dashboard/UserDashboardScreen.kt:55,73,81,173,204` and `user/data/local/db/UserDatabase.kt:5,8,9` — three languages in one repository

Comments in `UserDashboardScreen` are in Russian; `UserDatabase.kt` carries Ukrainian
ones (`// Імпортуйте це`, `// Додайте імпорт`) which are notes to yourself rather than
documentation. The rest of the codebase is English.

Pick one language and stay in it. English is the safest default for code you may one day
show to an employer.

---
---

# Module `:auth`

## Blocking

### AUTH-B1 · `data/local/entity/AuthCredentialEntity.kt`, `data/local/dao/AuthDao.kt:12,21` — nothing enforces that an email is unique

```kotlin
@Entity(tableName = "auth_credentials")
data class AuthCredentialEntity(
    @PrimaryKey val id: String,
    val email: String,          // no index, no unique constraint
    val passwordHash: String
)
```

Two bugs come out of this single omission.

**Duplicate accounts.** Registration checks and then acts, with nothing backing the
check:

```kotlin
if (authRepository.checkEmailExists(email)) return AuthResult.Error("User already exists")
...
authRepository.saveCredentials(newUserId, email, hashedPassword)
```

Anything interleaving between those two lines produces two rows with the same email.
Look closely at `OnConflictStrategy.ABORT` on `insertCredentials` and ask what it is
actually protecting: it fires on the primary key, and the primary key is a freshly
generated `UUID` every single time. It can never collide, so it can never fire. Once two
rows share an email, `getPasswordHashByEmail` returns whichever one SQLite hands back,
and the user can log in with one password but not the other.

**Emails are never normalised.** Register as `Daniil@Example.com`, log in as
`daniil@example.com`, and `WHERE email = :email` matches nothing — "user does not exist"
for an account that plainly does. SQLite compares `TEXT` case-sensitively by default.
This one needs no race at all; a real person on a phone keyboard will hit it.

## Medium

### AUTH-M1 · `domain/usecase/RefreshTokenUseCase.kt` — refreshing a token silently strips the user's roles

```kotlin
val newAccessToken = jwtProvider.generateAccessToken(userId, listOf("USER"))
```

The roles are hardcoded. A Doctor or Patient who refreshes drops to `USER` in the new
access token.

The damage is currently hidden because the UI reads roles from the Room database rather
than from the token — so the menu still looks right. Against a real backend, which would
trust the token, this is an authorisation bug.

Note also the spelling: this writes `"USER"` while your enum constant is `User`. That
mismatch is what makes `USER-M2` dangerous.

### AUTH-M2 · `security/crypto/PasswordHasher.kt`, `test/domain/usecase/RegisterUseCaseTest.kt` — security-relevant code with no tests

`PasswordHasher` has no tests at all. It is the class standing between a stolen database
and every user's password, and nothing verifies that it behaves. Think about what
properties it must have — including one that is easy to state and easy to get wrong
about salting.

`RegisterUseCaseTest` has 2 tests, covering neither the duplicate-email nor the
invalid-email path, though the specification names both as user-visible errors. Note
that the duplicate-email path is the same hole as `AUTH-B1`, seen from the other side:
the missing constraint and the missing test are one gap.

### AUTH-M3 · `LoginUseCase.kt`, `RegisterUseCase.kt`, `RefreshTokenUseCase.kt`, `domain/model/AuthResult.kt` — three use cases, three different error strategies

Same module, same layer, three approaches:

| Use case | Failure shape |
|---|---|
| `LoginUseCase` | 3 typed early returns + catch-all `Exception` |
| `RegisterUseCase` | 1 early return + catch-all `Exception` |
| `RefreshTokenUseCase` | returns `null`, four different causes, no try/catch at all |

`RefreshTokenUseCase` is the outlier that matters. `null` means "no token stored", "token
invalid", "token expired", or "no subject claim" — and `saveTokens` sits outside any
guard, so unlike its siblings an exception there escapes to the caller. But the caller
must react differently to each: no token means show Landing; expired means force logout
and say the session ended; a bad signature means force logout and treat it as suspicious.
Today all of them render the same.

**The payload is the deeper problem.** `AuthResult.Error(String)` puts user-facing copy in
the domain layer:

```kotlin
AuthResult.Error("User already exists")
AuthResult.Error("Registration failed: ${e.message}")
```

Three consequences. It cannot be localised — and Task 2 introduces an application
language from Remote Config, so this is already pointing the wrong way. `e.message` leaks
internals to the user; a Room constraint violation would surface raw SQL in a Snackbar.
And the ViewModel cannot branch on the failure, only print it.

Two smaller things in the same area. `LoginUseCase` and `RegisterUseCase` are
`suspend operator fun invoke`, `RefreshTokenUseCase` is a plain `operator fun invoke()` —
`operator invoke` is fine, but pick one shape. And returning `"Incorrect input"` for an
unknown email but `"Wrong password"` for a bad password lets an attacker discover which
addresses are registered; the task text suggested both messages, so you followed the
brief, but you should know why production systems return one identical message for both.

### AUTH-M4 · `security/jwt/JwtService.kt:65-75` — every validation failure becomes `null`

```kotlin
return try {
    verifier.verify(token)
} catch (e: Exception) {
    null
}
```

An expired token and a forged signature are not the same event. The first is routine and
expected. The second means someone is tampering with your tokens. Both come back as
`null`, so `UserDashboardViewModel` cannot tell them apart and has to guess.

### AUTH-M5 · `security/jwt/JwtService.kt:15,17,42,55` — hardcoded configuration and untestable token lifetimes

The issuer, the `"RS256"` / `"SHA256withRSA"` pair (written three times), the `"roles"`
claim key, and both token lifetimes are inline literals. The lifetimes are also
inconsistent in type — `15 * 60 * 1000` is an `Int`, `30L * 24 * 60 * 60 * 1000` is a
`Long`.

This is Medium rather than Minor for one reason: **you cannot test expiry.** Verifying
that a token expires would mean waiting fifteen real minutes, which is why
`JwtProviderTest` has no expiry test — a gap in the module the specification says needs
high coverage. Ask what would have to change about this class for that test to be
possible.

### AUTH-M6 · `RegisterUseCase.kt:5-6`, `LoginUseCase.kt:5-6` — domain use cases depending on concrete classes

```kotlin
class RegisterUseCase(
    private val authRepository: AuthRepository,   // interface
    private val jwtProvider: JwtProvider          // concrete class
)
```

Both live in `domain/usecase` but import `security.crypto.PasswordHasher` — an `object`,
so it cannot be substituted at all — and the concrete `JwtProvider`. The layer boundary
your package structure advertises is not actually enforced by anything.

This is also why testing them is awkward: every test runs real BCrypt at cost 12, and
`JwtProvider` drags in `RsaKeyManager` and the Android keystore.

Worth reflecting on more broadly: each of these use cases is called by exactly one
ViewModel and is thin orchestration. Ask yourself what they are earning. There *is* a
good answer here — but if you cannot articulate it, that is worth noticing.

### AUTH-M7 · `security/crypto/RsaKeyManager.kt:11-16` — keystore I/O and key generation in a constructor

```kotlin
private val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }

init {
    generateKey()
}
```

Constructing this object loads the Android keystore and, on first run, generates a
2048-bit RSA keypair. It is registered as a Koin `single`, so this happens on whichever
thread first asks for it — which, through `NavigationController`, is the main thread
during first composition.

More generally: think about what it means for a constructor to have side effects, and
what that does to your ability to create the object in a test.

## Minor

### AUTH-N1 · `security/crypto/RsaKeyManager.kt:12,28-30,47` — magic values and constant naming

The key alias, the key size `2048`, the digest and the signature padding are all inline.
Only `keyStoreType` made it to the companion object, and it is named in camelCase where
Kotlin convention for a `const val` is upper snake case.

There is also a testability angle worth spotting: with the alias fixed as a hard
constant, a test cannot avoid touching the real signing key.

---
---

# Module `:user`

## Medium

### USER-M1 · `data/local/dao/UserDao.kt:16-18` — the UI cannot observe its own writes

```kotlin
@Transaction
@Query("SELECT * FROM users WHERE id = :userId")
suspend fun getUserWithProfiles(userId: String): UserWithProfile?
```

Every read is a one-shot snapshot. Nothing is notified when the database changes, so the
UI only updates because something explicitly asked it to. That is why `becomeDoctor` and
`becomePatient` both end with a manual `fetchUser()` — without it, the side menu would
never notice the new role, which the specification requires it to.

The problem is the rule this imposes on the whole codebase: *every write must be followed
by a re-read*. Miss one and the screen silently shows stale data. That class of bug does
not announce itself, and it gets more likely with every screen you add.

Room has a well-known answer to this. Find it.

### USER-M2 · `data/repository/UserRepositoryImpl.kt` — `Role.valueOf` crashes on unrecognised input

```kotlin
roles = userWithProfile.user.roles.map { Role.valueOf(it) }
```

`Role.valueOf` throws `IllegalArgumentException` on anything that is not exactly
`Patient`, `Doctor` or `User`. This runs on **every read of the user**, so one bad row
makes that account permanently unopenable — and it fails as a crash, not as an error the
user could act on.

This is not hypothetical. `Converters` stores roles as a comma-joined string, so the
database enforces nothing about their contents. And the codebase already disagrees with
itself about spelling: `RefreshTokenUseCase` writes `"USER"` while the enum constant is
`User` (`AUTH-M1`).

When you fix this, there is a decision to make about what to do with a role you cannot
parse. Think carefully about which direction is safe for something that controls access,
and why the two options are not equally acceptable.

### USER-M3 · `data/repository/UserRepositoryImpl.kt` — constructor parameter named after its own type

```kotlin
class UserRepositoryImpl(
    private val UserDao: UserDao
) : UserRepository {
    ...
    val userWithProfile = UserDao.getUserWithProfiles(userId) ?: return null
```

The property is named `UserDao`, identical to its type and capitalised like a class. Call
sites then read as though `UserDao` were an object with static methods rather than an
injected instance. Kotlin allows it, but inside this class the identifier now shadows the
type name.

### USER-M4 · `test/data/repository/UserRepositoryImplTest.kt` — thin coverage

5 tests for 4 repository functions, and no DAO tests at all. `getUserWithProfiles` — the
one query with a `@Transaction` and two `@Relation`s, which is the only genuinely tricky
persistence code in the module — is never exercised against a real database.

---
---

# Project-wide

## Medium

### PROJ-M1 · `app/di/AppModule.kt`, `auth/di/AuthModule.kt`, `user/di/UserModule.kt` — interface binding through a cast

```kotlin
single { AuthRepositoryImpl(get(), get()) as AuthRepository }
```

To be clear, this works, and it is not a bug: Koin keys a definition by the lambda's
inferred return type, and the cast supplies it. All three modules resolve correctly.

The problem is the failure mode. The binding type is a consequence of a trailing
expression at the end of a line. Delete the cast during a refactor and the module still
compiles — but every `get<AuthRepository>()` becomes a runtime crash instead of a
compile error. Koin resolves lazily, so that crash arrives whenever the screen is first
opened, not at build time.

Ask whether there is a way to declare the binding so that mistake cannot compile.

## Minor

### PROJ-N1 · all three modules, `src/test` and `src/androidTest` — six template test stubs

`ExampleUnitTest` and `ExampleInstrumentedTest` in `:app`, `:auth` and `:user`. They
assert `2 + 2 == 4`. They inflate the test count without testing anything, which is
actively misleading when someone is assessing coverage.

### PROJ-N2 · `gradle/libs.versions.toml:2` — the project does not sync out of the box

AGP 9.3.1 is newer than the latest stable Android Studio supports, so opening this
project fails with *"The project is using an incompatible version of the Android Gradle
plugin"*. Anyone who clones your repository hits this before they can read a line of
code.

### PROJ-N3 · repository root — no README

Nothing tells a reader how to build the project, what the three modules are for, or which
Android Studio version is required.

---
---

## Reviewed and accepted — do not change these

- **RSA keys in `AndroidKeyStore` rather than PEM files.** You asked, and it is approved.
  It is the stronger choice. Be aware of what it costs: the keypair is per-device and
  there is no exportable public key, so a real backend could never verify these tokens.
- **`EncryptedSharedPreferences` is deprecated**, and the compiler will warn you about it
  ten times. Accepted for this project — it still works, and choosing it over plain
  `SharedPreferences` was the right instinct.
- **`TokenStorage` does not need its own tests.** Its three interactions are already
  verified in `AuthRepositoryImplTest`. Good judgement.
- **One `UserDao` and one `UserRepository` covering user, doctor and patient is correct.**
  It is not an Interface Segregation violation. `User` is the aggregate root and the
  profiles are 1:1 extensions keyed by `userId` with no independent lifecycle. Splitting
  them per entity would make things worse, not better.
- **Your `AuthDao` queries are not vulnerable to SQL injection.** `:email` is a bound
  parameter — Room compiles the statement once and binds the value separately, so it can
  never be interpreted as SQL. Room also validates every `@Query` against your schema at
  compile time.
- **The Material theme is used correctly** — one hardcoded text style in the entire
  `feature/` tree, everything else through `MaterialTheme`.
- **`ksp = "2.3.6"`** looks wrong against the old KSP versioning scheme but is correct;
  KSP moved to standalone versions.
