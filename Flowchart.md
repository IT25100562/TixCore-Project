# TixCore Spring Boot MVC Architecture
> Each domain flows **Controller → Service → Repository → Model**

---

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "primaryColor": "#0f172a",
    "primaryTextColor": "#f8fafc",
    "primaryBorderColor": "#334155",
    "lineColor": "#38bdf8",
    "secondaryColor": "#1e293b",
    "tertiaryColor": "#0f172a",
    "background": "#020617",
    "mainBkg": "#0f172a",
    "nodeBorder": "#38bdf8",
    "clusterBkg": "#1e293b",
    "titleColor": "#f8fafc",
    "edgeLabelBackground": "#1e293b",
    "fontFamily": "monospace"
  }
}}%%

flowchart LR

  subgraph CTRL["Controller"]
    direction TB
    C1["AdminController\n/admin/**"]
    C2["AuthController\n/auth/**"]
    C3["EventController\n/event/**"]
    C4["BookingController\n/booking/**"]
    C5["SeatApiController\n/api/seat/**"]
    C6["ReviewController\n/review/**"]
    C7["VenueController\n/venue/**"]
  end

  subgraph SVC["Service"]
    direction TB
    S1["SeatService\nlock · unlock · book"]
  end

  subgraph REPO["Repository"]
    direction TB
    R1["AdminRepository"]
    R2["UserRepository"]
    R3["EventRepository"]
    R4["BookingRepository"]
    R5["SeatRepository"]
    R6["ReviewRepository"]
    R7["VenueRepository"]
  end

  subgraph MDL["Model"]
    direction TB
    M8(["Entity\n«abstract»"])
    M1["Admin"]
    M2["User"]
    M3["Event"]
    M4["Booking"]
    M5["Seat"]
    M6["Review"]
    M7["Venue"]
  end

  C1 --> R1
  C2 --> R2
  C3 --> R3
  C3 --> R7
  C4 --> R4
  C4 --> S1
  C5 --> S1
  C6 --> R6
  C7 --> R7

  S1 --> R5

  R1 --> M1
  R2 --> M2
  R3 --> M3
  R4 --> M4
  R5 --> M5
  R6 --> M6
  R7 --> M7

  M1 & M2 & M3 & M4 & M5 & M6 & M7 -.->|extends| M8

  classDef ctrl  fill:#0369a1,stroke:#38bdf8,color:#f0f9ff
  classDef svc   fill:#065f46,stroke:#34d399,color:#ecfdf5
  classDef repo  fill:#7c3aed,stroke:#a78bfa,color:#f5f3ff
  classDef model fill:#92400e,stroke:#fbbf24,color:#fffbeb
  classDef base  fill:#1c1917,stroke:#fbbf24,color:#fbbf24,stroke-dasharray:4 2

  class C1,C2,C3,C4,C5,C6,C7 ctrl
  class S1 svc
  class R1,R2,R3,R4,R5,R6,R7 repo
  class M1,M2,M3,M4,M5,M6,M7 model
  class M8 base
```

---

## Layer Summary

| Layer | Classes | Responsibility |
|-------|---------|----------------|
| **Controller**  | 7 controllers | HTTP endpoints & request routing |
| **Service**  | SeatService | Seat locking & concurrency logic |
| **Repository**  | 7 repositories | CSV-based persistence via FileRepository |
| **Model**  | 7 models + Entity | Domain objects, all extend abstract `Entity` |
