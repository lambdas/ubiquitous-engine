package object models {
  
  type Activity = String
  type DirectFollowerMatrix = Map[(Activity, Activity), Int]
}
