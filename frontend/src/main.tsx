
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { Suspense } from "react";
import { router } from "./router/index.tsx";
import "./index.css";

createRoot(document.getElementById("root")!).render(
  <Suspense fallback={<div>Loading...</div>}>
    <RouterProvider router={router} />
  </Suspense>
);
  