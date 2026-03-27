import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import htm from "https://esm.sh/htm@3.1.1";

const html = htm.bind(React.createElement);

export { React, createRoot, html, useEffect, useState };
